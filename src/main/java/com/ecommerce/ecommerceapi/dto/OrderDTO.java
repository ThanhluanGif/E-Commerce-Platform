package com.ecommerce.ecommerceapi.dto;

import com.ecommerce.ecommerceapi.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDTO {
    private Integer id;
    private String orderCode;
    private BigDecimal totalPrice;
    private OrderStatus status;
    private String shippingAddress;
    private String paymentMethod;
    private LocalDateTime createdAt;
    private Integer userId;
    private String username;
    private List<OrderItemDTO> items;
}
