package com.tunombre.tienda.service;

import com.tunombre.tienda.dto.OrderCreateRequestDTO;
import com.tunombre.tienda.dto.OrderResponseDTO;
import com.tunombre.tienda.entity.Orders;
import com.tunombre.tienda.entity.Products;
import com.tunombre.tienda.entity.Users;
import com.tunombre.tienda.enums.OrderStatusEnum;
import com.tunombre.tienda.enums.RoleEnum;
import com.tunombre.tienda.exception.BadRequestException;
import com.tunombre.tienda.exception.ForbiddenException;
import com.tunombre.tienda.exception.ResourceNotFoundException;
import com.tunombre.tienda.repository.OrdersRepository;
import com.tunombre.tienda.repository.ProductsRepository;
import com.tunombre.tienda.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrdersService {

    private final OrdersRepository ordersRepository;
    private final ProductsRepository productsRepository;
    private final UsersRepository usersRepository;
    private final ProductsService productsService;

    @Transactional
    public OrderResponseDTO createOrder(OrderCreateRequestDTO dto, Long clientId) {
        Users client = usersRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + clientId));

        Products product = productsRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + dto.getProductId()));

        if (product.getStock() < dto.getQuantity()) {
            throw new BadRequestException("Stock insuficiente para el producto '" + product.getName() + "'. Stock actual: " + product.getStock());
        }

        // Descontar stock
        product.setStock(product.getStock() - dto.getQuantity());
        productsRepository.save(product);

        // El servidor calcula el total: price * quantity
        BigDecimal total = product.getPrice().multiply(BigDecimal.valueOf(dto.getQuantity()));

        Orders order = Orders.builder()
                .client(client)
                .product(product)
                .quantity(dto.getQuantity())
                .total(total)
                .status(OrderStatusEnum.PENDIENTE)
                .build();

        Orders saved = ordersRepository.save(order);
        return toResponseDTO(saved);
    }

    public List<OrderResponseDTO> getMyOrders(Long clientId) {
        return ordersRepository.findByClientId(clientId).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<OrderResponseDTO> getSalesOrders(Long sellerId) {
        return ordersRepository.findByProductSellerId(sellerId).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public List<OrderResponseDTO> getAllOrders() {
        return ordersRepository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponseDTO updateStatus(Long orderId, OrderStatusEnum newStatus, Long currentUserId, Integer currentRolId) {
        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con id: " + orderId));

        // Regla de propiedad: Vendedor solo puede cambiar estado si el producto le pertenece
        if (currentRolId == RoleEnum.VENDEDOR.getId()) {
            if (!order.getProduct().getSeller().getId().equals(currentUserId)) {
                throw new ForbiddenException("No tienes permisos para modificar el estado de un pedido ajeno a tus productos");
            }
        }

        order.setStatus(newStatus);
        Orders updated = ordersRepository.save(order);
        return toResponseDTO(updated);
    }

    @Transactional
    public OrderResponseDTO cancelOrder(Long orderId, Long currentClientId) {
        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con id: " + orderId));

        // Regla de propiedad: Solo el cliente dueño del pedido puede cancelarlo
        if (!order.getClient().getId().equals(currentClientId)) {
            throw new ForbiddenException("No tienes permisos para cancelar un pedido que no te pertenece");
        }

        // Solo se puede cancelar si está en estado PENDIENTE
        if (order.getStatus() != OrderStatusEnum.PENDIENTE) {
            throw new BadRequestException("Solo se pueden cancelar pedidos en estado PENDIENTE. Estado actual: " + order.getStatus());
        }

        // Devolver el stock al producto
        Products product = order.getProduct();
        product.setStock(product.getStock() + order.getQuantity());
        productsRepository.save(product);

        order.setStatus(OrderStatusEnum.CANCELADO);
        Orders updated = ordersRepository.save(order);
        return toResponseDTO(updated);
    }

    public OrderResponseDTO toResponseDTO(Orders order) {
        return OrderResponseDTO.builder()
                .id(order.getId())
                .client(AuthService.toUserResponseDTO(order.getClient()))
                .product(productsService.toResponseDTO(order.getProduct()))
                .quantity(order.getQuantity())
                .total(order.getTotal())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .build();
    }
}
