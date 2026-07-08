package com.ecommerce.order.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttributeResponse {
    private Long attributeId;
    private String attributeName;
    private Long valueId;
    private String value;
}
