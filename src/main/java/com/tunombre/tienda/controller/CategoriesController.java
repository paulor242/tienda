package com.tunombre.tienda.controller;

import com.tunombre.tienda.dto.CategoryRequestDTO;
import com.tunombre.tienda.dto.CategoryResponseDTO;
import com.tunombre.tienda.dto.HttpGlobalResponseDTO;
import com.tunombre.tienda.enums.RoleEnum;
import com.tunombre.tienda.security.RequiresRole;
import com.tunombre.tienda.service.CategoriesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoriesController {

    private final CategoriesService categoriesService;

    @GetMapping
    @RequiresRole({RoleEnum.ADMIN, RoleEnum.CLIENTE, RoleEnum.VENDEDOR})
    public ResponseEntity<HttpGlobalResponseDTO<List<CategoryResponseDTO>>> getAllCategories() {
        List<CategoryResponseDTO> categories = categoriesService.getAllCategories();
        return ResponseEntity.ok(HttpGlobalResponseDTO.<List<CategoryResponseDTO>>builder()
                .message("Listado de categorías obtenido correctamente")
                .data(categories)
                .build());
    }

    @PostMapping
    @RequiresRole(RoleEnum.ADMIN)
    public ResponseEntity<HttpGlobalResponseDTO<CategoryResponseDTO>> createCategory(
            @Valid @RequestBody CategoryRequestDTO dto) {
        CategoryResponseDTO created = categoriesService.createCategory(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(HttpGlobalResponseDTO.<CategoryResponseDTO>builder()
                        .message("Categoría creada exitosamente")
                        .data(created)
                        .build());
    }

    @DeleteMapping("/{id}")
    @RequiresRole(RoleEnum.ADMIN)
    public ResponseEntity<HttpGlobalResponseDTO<Void>> deleteCategory(@PathVariable Integer id) {
        categoriesService.deleteCategory(id);
        return ResponseEntity.ok(HttpGlobalResponseDTO.<Void>builder()
                .message("Categoría eliminada exitosamente")
                .data(null)
                .build());
    }
}
