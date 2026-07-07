package com.ecommerce.ecommerceapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlashSaleItemDTO {
    private Integer id;
    private Integer productId;
    private String productName;
    private String productImageUrl;
    private BigDecimal salePrice;
    private Integer saleQuantity;
    private Integer soldCount;
}
