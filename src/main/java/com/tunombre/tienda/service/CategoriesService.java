package com.tunombre.tienda.service;

import com.tunombre.tienda.dto.CategoryRequestDTO;
import com.tunombre.tienda.dto.CategoryResponseDTO;
import com.tunombre.tienda.entity.Categories;
import com.tunombre.tienda.exception.ConflictException;
import com.tunombre.tienda.exception.ResourceNotFoundException;
import com.tunombre.tienda.repository.CategoriesRepository;
import com.tunombre.tienda.repository.ProductsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoriesService {

    private final CategoriesRepository categoriesRepository;
    private final ProductsRepository productsRepository;

    public List<CategoryResponseDTO> getAllCategories() {
        return categoriesRepository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public CategoryResponseDTO createCategory(CategoryRequestDTO dto) {
        if (categoriesRepository.existsByName(dto.getName())) {
            throw new ConflictException("La categoría '" + dto.getName() + "' ya existe");
        }

        Categories category = Categories.builder()
                .name(dto.getName())
                .build();

        Categories saved = categoriesRepository.save(category);
        return toResponseDTO(saved);
    }

    public void deleteCategory(Integer id) {
        Categories category = categoriesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + id));

        if (productsRepository.existsByCategoryId(id)) {
            throw new ConflictException("No es posible eliminar la categoría porque tiene productos asociados");
        }

        categoriesRepository.delete(category);
    }

    public CategoryResponseDTO toResponseDTO(Categories category) {
        return CategoryResponseDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }
}
