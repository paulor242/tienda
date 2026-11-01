package com.tunombre.tienda.repository;

import com.tunombre.tienda.entity.Products;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductsRepository extends JpaRepository<Products, Long> {
    List<Products> findByCategoryId(Integer categoryId);
    Page<Products> findByCategoryId(Integer categoryId, Pageable pageable);
    List<Products> findBySellerId(Long sellerId);
    boolean existsByCategoryId(Integer categoryId);
}
