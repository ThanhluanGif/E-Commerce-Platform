package com.ecommerce.product.dto;

import com.ecommerce.product.entity.ProductStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private Long brandId;
    private String brandName;
    private String name;
    private String slug;
    private String sku;
    private String description;
    private String shortDescription;
    private String thumbnailUrl;
    private ProductStatus status;
    private Integer viewCount;
    private BigDecimal ratingAvg;
    private Integer ratingCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ProductVariantResponse> variants;
}
