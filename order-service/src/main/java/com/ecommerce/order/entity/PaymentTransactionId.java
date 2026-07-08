package com.ecommerce.order.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransactionId implements Serializable {
    private Long id;
    private LocalDateTime orderCreatedAt;
}
