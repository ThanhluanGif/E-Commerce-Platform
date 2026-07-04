package com.ecommerce.ecommerceapi.repository;

import com.ecommerce.ecommerceapi.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecommerce.ecommerceapi.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByUserIdOrderByCreatedAtDesc(Integer userId);
    Optional<Order> findByOrderCode(String orderCode);
    List<Order> findByStatus(OrderStatus status);

    @Query("SELECT DISTINCT o FROM Order o JOIN o.orderItems oi WHERE oi.product.shop.id = :shopId")
    Page<Order> findByShopId(@Param("shopId") Integer shopId, Pageable pageable);

    @Query("SELECT DISTINCT o FROM Order o JOIN o.orderItems oi WHERE oi.product.shop.id = :shopId ORDER BY o.createdAt DESC")
    List<Order> findByShopIdOrderByCreatedAtDesc(@Param("shopId") Integer shopId);
}
