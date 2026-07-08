package com.ecommerce.order.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderId implements Serializable {
    private Long id;
    private LocalDateTime createdAt;
}
