package com.ecommerce.product.dto;

import com.ecommerce.product.entity.ProductStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreateRequest {
    @NotNull(message = "Category ID cannot be null")
    private Long categoryId;

    @NotNull(message = "Brand ID cannot be null")
    private Long brandId;

    @NotBlank(message = "Product name cannot be blank")
    private String name;

    @NotBlank(message = "Product slug cannot be blank")
    private String slug;

    @NotBlank(message = "Product SKU cannot be blank")
    private String sku;

    private String description;
    private String shortDescription;
    private String thumbnailUrl;

    @Builder.Default
    private ProductStatus status = ProductStatus.DRAFT;

    @NotEmpty(message = "Product must have at least one variant")
    @Valid
    @Builder.Default
    private List<ProductVariantCreateRequest> variants = new ArrayList<>();
}
