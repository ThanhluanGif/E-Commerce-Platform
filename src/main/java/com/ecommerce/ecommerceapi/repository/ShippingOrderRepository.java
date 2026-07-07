package com.ecommerce.ecommerceapi.repository;

import com.ecommerce.ecommerceapi.entity.ShippingOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShippingOrderRepository extends JpaRepository<ShippingOrder, Integer> {
    Optional<ShippingOrder> findByOrderId(Integer orderId);
    Optional<ShippingOrder> findByTrackingCode(String trackingCode);
}
