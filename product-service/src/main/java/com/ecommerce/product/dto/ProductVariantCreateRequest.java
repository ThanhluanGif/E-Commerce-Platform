package com.ecommerce.product.dto;

import com.ecommerce.product.entity.VariantStatus;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantCreateRequest {
    @NotBlank(message = "Variant SKU cannot be blank")
    private String sku;

    @NotBlank(message = "Variant name cannot be blank")
    private String name;

    @NotNull(message = "Variant price cannot be null")
    @PositiveOrZero(message = "Variant price must be zero or positive")
    private BigDecimal price;

    private BigDecimal compareAtPrice;

    @Builder.Default
    private Integer lowStockThreshold = 5;

    @Builder.Default
    private Integer weightGrams = 0;

    @Builder.Default
    private Integer lengthCm = 0;

    @Builder.Default
    private Integer widthCm = 0;

    @Builder.Default
    private Integer heightCm = 0;

    @Builder.Default
    private VariantStatus status = VariantStatus.ACTIVE;

    @Builder.Default
    private List<AttributeMappingRequest> attributes = new ArrayList<>();
}
