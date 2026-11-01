package com.tunombre.tienda.controller;

import com.tunombre.tienda.dto.HttpGlobalResponseDTO;
import com.tunombre.tienda.dto.ProductRequestDTO;
import com.tunombre.tienda.dto.ProductResponseDTO;
import com.tunombre.tienda.enums.RoleEnum;
import com.tunombre.tienda.security.RequiresRole;
import com.tunombre.tienda.service.ProductsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductsController {

    private final ProductsService productsService;

    // Catálogo público con filtro opcional de categoría y paginación (Reto 4)
    @GetMapping
    public ResponseEntity<HttpGlobalResponseDTO<Page<ProductResponseDTO>>> getCatalog(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<ProductResponseDTO> catalog = productsService.getCatalog(categoryId, pageable);

        return ResponseEntity.ok(HttpGlobalResponseDTO.<Page<ProductResponseDTO>>builder()
                .message("Catálogo de productos obtenido correctamente")
                .data(catalog)
                .build());
    }

    // Detalle público de producto
    @GetMapping("/{id}")
    public ResponseEntity<HttpGlobalResponseDTO<ProductResponseDTO>> getProductById(@PathVariable Long id) {
        ProductResponseDTO product = productsService.getById(id);
        return ResponseEntity.ok(HttpGlobalResponseDTO.<ProductResponseDTO>builder()
                .message("Detalle del producto obtenido correctamente")
                .data(product)
                .build());
    }

    // Productos propios del vendedor
    @GetMapping("/mine")
    @RequiresRole(RoleEnum.VENDEDOR)
    public ResponseEntity<HttpGlobalResponseDTO<List<ProductResponseDTO>>> getMyProducts(HttpServletRequest request) {
        Long sellerId = (Long) request.getAttribute("userId");
        List<ProductResponseDTO> products = productsService.getMyProducts(sellerId);
        return ResponseEntity.ok(HttpGlobalResponseDTO.<List<ProductResponseDTO>>builder()
                .message("Mis productos obtenidos correctamente")
                .data(products)
                .build());
    }

    // Publicación de producto por el vendedor (seller_id se toma del token)
    @PostMapping
    @RequiresRole(RoleEnum.VENDEDOR)
    public ResponseEntity<HttpGlobalResponseDTO<ProductResponseDTO>> createProduct(
            @Valid @RequestBody ProductRequestDTO dto,
            HttpServletRequest request) {

        Long sellerId = (Long) request.getAttribute("userId");
        ProductResponseDTO created = productsService.createProduct(dto, sellerId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(HttpGlobalResponseDTO.<ProductResponseDTO>builder()
                        .message("Producto creado exitosamente")
                        .data(created)
                        .build());
    }

    // Edición de producto (VENDEDOR solo si es dueño, ADMIN libremente)
    @PutMapping("/{id}")
    @RequiresRole({RoleEnum.VENDEDOR, RoleEnum.ADMIN})
    public ResponseEntity<HttpGlobalResponseDTO<ProductResponseDTO>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequestDTO dto,
            HttpServletRequest request) {

        Long currentUserId = (Long) request.getAttribute("userId");
        Integer currentRolId = (Integer) request.getAttribute("rolId");

        ProductResponseDTO updated = productsService.updateProduct(id, dto, currentUserId, currentRolId);

        return ResponseEntity.ok(HttpGlobalResponseDTO.<ProductResponseDTO>builder()
                .message("Producto actualizado exitosamente")
                .data(updated)
                .build());
    }

    // Eliminación de producto (VENDEDOR solo si es dueño, ADMIN libremente)
    @DeleteMapping("/{id}")
    @RequiresRole({RoleEnum.VENDEDOR, RoleEnum.ADMIN})
    public ResponseEntity<HttpGlobalResponseDTO<Void>> deleteProduct(
            @PathVariable Long id,
            HttpServletRequest request) {

        Long currentUserId = (Long) request.getAttribute("userId");
        Integer currentRolId = (Integer) request.getAttribute("rolId");

        productsService.deleteProduct(id, currentUserId, currentRolId);

        return ResponseEntity.ok(HttpGlobalResponseDTO.<Void>builder()
                .message("Producto eliminado exitosamente")
                .data(null)
                .build());
    }
}
