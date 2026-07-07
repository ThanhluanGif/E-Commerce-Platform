package com.ecommerce.product.dto;

import com.ecommerce.product.entity.ProductStatus;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductUpdateRequest {
    private Long categoryId;
    private Long brandId;
    private String name;
    private String slug;
    private String sku;
    private String description;
    private String shortDescription;
    private String thumbnailUrl;
    private ProductStatus status;
}
