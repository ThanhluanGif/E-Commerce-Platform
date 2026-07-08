package com.ecommerce.order.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCouponId implements Serializable {
    private Long orderId;
    private Long couponId;
    private LocalDateTime orderCreatedAt;
}
