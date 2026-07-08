package com.ecommerce.product.document;

import lombok.*;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariantDocument {

    private Long id;

    @Field(type = FieldType.Keyword)
    private String sku;

    @Field(type = FieldType.Text, analyzer = "vi_analyzer", searchAnalyzer = "vi_analyzer")
    private String name;

    @Field(type = FieldType.Double)
    private BigDecimal price;

    @Field(type = FieldType.Double)
    private BigDecimal compareAtPrice;

    @Field(type = FieldType.Integer)
    private Integer lowStockThreshold;

    @Field(type = FieldType.Integer)
    private Integer weightGrams;

    @Field(type = FieldType.Integer)
    private Integer lengthCm;

    @Field(type = FieldType.Integer)
    private Integer widthCm;

    @Field(type = FieldType.Integer)
    private Integer heightCm;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Nested)
    private List<ProductVariantAttributeDocument> attributes;
}
