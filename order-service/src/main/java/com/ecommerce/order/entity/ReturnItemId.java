package com.ecommerce.order.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReturnItemId implements Serializable {
    private Long id;
    private LocalDateTime orderCreatedAt;
}
