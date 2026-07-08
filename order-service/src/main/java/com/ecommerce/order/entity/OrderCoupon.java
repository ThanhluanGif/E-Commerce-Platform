package com.ecommerce.order.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_coupons", schema = "orders")
@IdClass(OrderCouponId.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCoupon {

    @Id
    @Column(name = "order_id")
    private Long orderId;

    @Id
    @Column(name = "coupon_id")
    private Long couponId;

    @Id
    @Column(name = "order_created_at")
    private LocalDateTime orderCreatedAt;
}
