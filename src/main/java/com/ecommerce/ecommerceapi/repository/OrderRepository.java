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

    @Query("SELECT SUM(o.totalPrice) FROM Order o WHERE o.status = 'DELIVERED' AND o.createdAt BETWEEN :start AND :end")
    java.math.BigDecimal calculateRevenue(
            @Param("start") java.time.LocalDateTime start,
            @Param("end") java.time.LocalDateTime end
    );

    @Query("SELECT o.status, COUNT(o.id) FROM Order o GROUP BY o.status")
    List<Object[]> getOrderStatusStats();

    @Query("SELECT SUM(o.totalPrice) FROM Order o JOIN o.orderItems oi " +
           "WHERE oi.product.shop.id = :shopId AND o.status = 'DELIVERED' AND o.createdAt BETWEEN :start AND :end")
    java.math.BigDecimal calculateShopRevenue(
            @Param("shopId") Integer shopId,
            @Param("start") java.time.LocalDateTime start,
            @Param("end") java.time.LocalDateTime end
    );
}
