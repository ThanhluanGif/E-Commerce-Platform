package com.ecommerce.product.mapper;

import com.ecommerce.product.document.ProductDocument;
import com.ecommerce.product.document.ProductVariantAttributeDocument;
import com.ecommerce.product.document.ProductVariantDocument;
import com.ecommerce.product.entity.Product;
import com.ecommerce.product.entity.ProductVariant;
import lombok.experimental.UtilityClass;
import java.util.stream.Collectors;

@UtilityClass
public class ProductDocumentMapper {

    public static ProductDocument toDocument(Product product) {
        if (product == null) {
            return null;
        }
        return ProductDocument.builder()
                .id(product.getId() != null ? product.getId().toString() : null)
                .name(product.getName())
                .slug(product.getSlug())
                .sku(product.getSku())
                .description(product.getDescription())
                .shortDescription(product.getShortDescription())
                .thumbnailUrl(product.getThumbnailUrl())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .brandId(product.getBrand() != null ? product.getBrand().getId() : null)
                .brandName(product.getBrand() != null ? product.getBrand().getName() : null)
                .status(product.getStatus() != null ? product.getStatus().name() : null)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .variants(product.getVariants() == null ? null : product.getVariants().stream()
                        .filter(v -> v.getDeletedAt() == null)
                        .map(ProductDocumentMapper::toVariantDocument)
                        .collect(Collectors.toList()))
                .build();
    }

    private static ProductVariantDocument toVariantDocument(ProductVariant v) {
        return ProductVariantDocument.builder()
                .id(v.getId())
                .sku(v.getSku())
                .name(v.getName())
                .price(v.getPrice())
                .compareAtPrice(v.getCompareAtPrice())
                .lowStockThreshold(v.getLowStockThreshold())
                .weightGrams(v.getWeightGrams())
                .lengthCm(v.getLengthCm())
                .widthCm(v.getWidthCm())
                .heightCm(v.getHeightCm())
                .status(v.getStatus() != null ? v.getStatus().name() : null)
                .attributes(v.getAttributeValues() == null ? null : v.getAttributeValues().stream()
                        .map(av -> ProductVariantAttributeDocument.builder()
                                .attributeId(av.getAttribute().getId())
                                .attributeName(av.getAttribute().getName())
                                .valueId(av.getId())
                                .value(av.getValue())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
