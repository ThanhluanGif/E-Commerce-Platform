package com.ecommerce.order.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {
    private Long variantId;
    private Integer quantity;
    private ProductVariantResponse variant;
}
