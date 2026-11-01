package com.tunombre.tienda.repository;

import com.tunombre.tienda.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdersRepository extends JpaRepository<Orders, Long> {
    List<Orders> findByClientId(Long clientId);
    List<Orders> findByProductSellerId(Long sellerId);
}
