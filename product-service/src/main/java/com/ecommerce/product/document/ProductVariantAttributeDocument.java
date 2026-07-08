package com.ecommerce.product.document;

import lombok.*;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariantAttributeDocument {

    @Field(type = FieldType.Long)
    private Long attributeId;

    @Field(type = FieldType.Keyword)
    private String attributeName;

    @Field(type = FieldType.Long)
    private Long valueId;

    @Field(type = FieldType.Keyword)
    private String value;
}
