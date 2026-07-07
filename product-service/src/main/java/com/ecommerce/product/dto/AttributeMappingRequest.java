package com.ecommerce.product.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttributeMappingRequest {
    @NotBlank(message = "Attribute name cannot be blank")
    private String name;

    @NotBlank(message = "Attribute value cannot be blank")
    private String value;
}
