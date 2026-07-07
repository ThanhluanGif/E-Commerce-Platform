package com.ecommerce.product.dto;

import com.ecommerce.product.entity.VariantStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantResponse {
    private Long id;
    private Long productId;
    private String sku;
    private String name;
    private BigDecimal price;
    private BigDecimal compareAtPrice;
    private Integer lowStockThreshold;
    private Integer weightGrams;
    private Integer lengthCm;
    private Integer widthCm;
    private Integer heightCm;
    private VariantStatus status;
    private Integer version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<AttributeResponse> attributes;
}
