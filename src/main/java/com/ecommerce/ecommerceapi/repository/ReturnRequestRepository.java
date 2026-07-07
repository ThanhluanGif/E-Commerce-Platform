package com.ecommerce.ecommerceapi.repository;

import com.ecommerce.ecommerceapi.entity.ReturnRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, Integer> {
    List<ReturnRequest> findByUserIdOrderByCreatedAtDesc(Integer userId);

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT rr FROM ReturnRequest rr " +
            "JOIN rr.order.orderItems oi " +
            "WHERE oi.product.shop.id = :shopId " +
            "ORDER BY rr.createdAt DESC")
    List<ReturnRequest> findByOrderShopIdOrderByCreatedAtDesc(@org.springframework.data.repository.query.Param("shopId") Integer shopId);

    Optional<ReturnRequest> findByOrderId(Integer orderId);
}
