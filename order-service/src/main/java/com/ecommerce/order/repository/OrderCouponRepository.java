package com.ecommerce.order.repository;

import com.ecommerce.order.entity.OrderCoupon;
import com.ecommerce.order.entity.OrderCouponId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderCouponRepository extends JpaRepository<OrderCoupon, OrderCouponId> {
    List<OrderCoupon> findByOrderId(Long orderId);
}
