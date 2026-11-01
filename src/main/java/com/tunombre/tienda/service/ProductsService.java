package com.tunombre.tienda.service;

import com.tunombre.tienda.dto.CategoryResponseDTO;
import com.tunombre.tienda.dto.ProductRequestDTO;
import com.tunombre.tienda.dto.ProductResponseDTO;
import com.tunombre.tienda.entity.Categories;
import com.tunombre.tienda.entity.Products;
import com.tunombre.tienda.entity.Users;
import com.tunombre.tienda.enums.RoleEnum;
import com.tunombre.tienda.exception.ForbiddenException;
import com.tunombre.tienda.exception.ResourceNotFoundException;
import com.tunombre.tienda.repository.CategoriesRepository;
import com.tunombre.tienda.repository.ProductsRepository;
import com.tunombre.tienda.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductsService {

    private final ProductsRepository productsRepository;
    private final CategoriesRepository categoriesRepository;
    private final UsersRepository usersRepository;

    public Page<ProductResponseDTO> getCatalog(Integer categoryId, Pageable pageable) {
        Page<Products> productsPage;
        if (categoryId != null) {
            productsPage = productsRepository.findByCategoryId(categoryId, pageable);
        } else {
            productsPage = productsRepository.findAll(pageable);
        }
        return productsPage.map(this::toResponseDTO);
    }

    public ProductResponseDTO getById(Long id) {
        Products product = productsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + id));
        return toResponseDTO(product);
    }

    public List<ProductResponseDTO> getMyProducts(Long sellerId) {
        return productsRepository.findBySellerId(sellerId).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public ProductResponseDTO createProduct(ProductRequestDTO dto, Long sellerId) {
        Users seller = usersRepository.findById(sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendedor no encontrado con id: " + sellerId));

        Categories category = categoriesRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + dto.getCategoryId()));

        Products product = Products.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .stock(dto.getStock())
                .category(category)
                .seller(seller)
                .build();

        Products saved = productsRepository.save(product);
        return toResponseDTO(saved);
    }

    public ProductResponseDTO updateProduct(Long productId, ProductRequestDTO dto, Long currentUserId, Integer currentRolId) {
        Products product = productsRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + productId));

        // Validación de propiedad: Vendedor solo puede editar sus propios productos
        if (currentRolId == RoleEnum.VENDEDOR.getId()) {
            if (!product.getSeller().getId().equals(currentUserId)) {
                throw new ForbiddenException("No tienes permisos para modificar un producto que no te pertenece");
            }
        }

        Categories category = categoriesRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + dto.getCategoryId()));

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setCategory(category);

        Products updated = productsRepository.save(product);
        return toResponseDTO(updated);
    }

    public void deleteProduct(Long productId, Long currentUserId, Integer currentRolId) {
        Products product = productsRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con id: " + productId));

        // Validación de propiedad: Vendedor solo puede eliminar sus propios productos
        if (currentRolId == RoleEnum.VENDEDOR.getId()) {
            if (!product.getSeller().getId().equals(currentUserId)) {
                throw new ForbiddenException("No tienes permisos para eliminar un producto que no te pertenece");
            }
        }

        productsRepository.delete(product);
    }

    public ProductResponseDTO toResponseDTO(Products product) {
        return ProductResponseDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .category(CategoryResponseDTO.builder()
                        .id(product.getCategory().getId())
                        .name(product.getCategory().getName())
                        .build())
                .seller(AuthService.toUserResponseDTO(product.getSeller()))
                .build();
    }
}
