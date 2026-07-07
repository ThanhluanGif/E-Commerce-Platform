package com.ecommerce.product.service.impl;

import com.ecommerce.common.exception.AppException;
import com.ecommerce.common.exception.ResourceNotFoundException;
import com.ecommerce.product.dto.*;
import com.ecommerce.product.entity.*;
import com.ecommerce.product.repository.*;
import com.ecommerce.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final AttributeRepository attributeRepository;
    private final AttributeValueRepository attributeValueRepository;

    @Override
    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + request.getCategoryId()));
        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with ID: " + request.getBrandId()));

        if (productRepository.existsBySkuAndDeletedAtIsNull(request.getSku())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Product SKU already exists: " + request.getSku());
        }
        if (productRepository.existsBySlugAndDeletedAtIsNull(request.getSlug())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Product Slug already exists: " + request.getSlug());
        }

        Product product = Product.builder()
                .category(category)
                .brand(brand)
                .name(request.getName())
                .slug(request.getSlug())
                .sku(request.getSku())
                .description(request.getDescription())
                .shortDescription(request.getShortDescription())
                .thumbnailUrl(request.getThumbnailUrl())
                .status(request.getStatus() != null ? request.getStatus() : ProductStatus.DRAFT)
                .build();

        product = productRepository.save(product);

        List<ProductVariant> variants = new ArrayList<>();
        for (ProductVariantCreateRequest vReq : request.getVariants()) {
            if (productVariantRepository.existsBySkuAndDeletedAtIsNull(vReq.getSku())) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Variant SKU already exists: " + vReq.getSku());
            }

            List<AttributeValue> attrValues = new ArrayList<>();
            for (AttributeMappingRequest attrReq : vReq.getAttributes()) {
                // Find or create Attribute
                Attribute attribute = attributeRepository.findByName(attrReq.getName())
                        .orElseGet(() -> attributeRepository.save(
                                Attribute.builder()
                                        .name(attrReq.getName())
                                        .isFilterable(true)
                                        .build()
                        ));

                // Find or create AttributeValue
                AttributeValue attributeValue = attributeValueRepository
                        .findByAttributeNameAndValue(attrReq.getName(), attrReq.getValue())
                        .orElseGet(() -> attributeValueRepository.save(
                                AttributeValue.builder()
                                        .attribute(attribute)
                                        .value(attrReq.getValue())
                                        .build()
                        ));

                attrValues.add(attributeValue);
            }

            ProductVariant variant = ProductVariant.builder()
                    .product(product)
                    .sku(vReq.getSku())
                    .name(vReq.getName())
                    .price(vReq.getPrice())
                    .compareAtPrice(vReq.getCompareAtPrice())
                    .lowStockThreshold(vReq.getLowStockThreshold())
                    .weightGrams(vReq.getWeightGrams())
                    .lengthCm(vReq.getLengthCm())
                    .widthCm(vReq.getWidthCm())
                    .heightCm(vReq.getHeightCm())
                    .status(vReq.getStatus())
                    .attributeValues(attrValues)
                    .build();

            variants.add(productVariantRepository.save(variant));
        }

        product.setVariants(variants);

        return mapToProductResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        return mapToProductResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        return productRepository.findByDeletedAtIsNull(pageable)
                .map(this::mapToProductResponse);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductUpdateRequest request) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + request.getCategoryId()));
            product.setCategory(category);
        }

        if (request.getBrandId() != null) {
            Brand brand = brandRepository.findById(request.getBrandId())
                    .orElseThrow(() -> new ResourceNotFoundException("Brand not found with ID: " + request.getBrandId()));
            product.setBrand(brand);
        }

        if (request.getName() != null) {
            product.setName(request.getName());
        }

        if (request.getSlug() != null) {
            if (!request.getSlug().equals(product.getSlug()) && productRepository.existsBySlugAndDeletedAtIsNull(request.getSlug())) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Product Slug already exists: " + request.getSlug());
            }
            product.setSlug(request.getSlug());
        }

        if (request.getSku() != null) {
            if (!request.getSku().equals(product.getSku()) && productRepository.existsBySkuAndDeletedAtIsNull(request.getSku())) {
                throw new AppException(HttpStatus.BAD_REQUEST, "Product SKU already exists: " + request.getSku());
            }
            product.setSku(request.getSku());
        }

        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }

        if (request.getShortDescription() != null) {
            product.setShortDescription(request.getShortDescription());
        }

        if (request.getThumbnailUrl() != null) {
            product.setThumbnailUrl(request.getThumbnailUrl());
        }

        if (request.getStatus() != null) {
            product.setStatus(request.getStatus());
        }

        Product saved = productRepository.save(product);
        return mapToProductResponse(saved);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        LocalDateTime now = LocalDateTime.now();
        product.setDeletedAt(now);
        product.setStatus(ProductStatus.ARCHIVED);

        for (ProductVariant variant : product.getVariants()) {
            variant.setDeletedAt(now);
        }

        productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductVariantResponse getVariantById(Long id) {
        ProductVariant variant = productVariantRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Variant not found with ID: " + id));
        return mapToProductVariantResponse(variant);
    }

    private ProductResponse mapToProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .brandId(product.getBrand().getId())
                .brandName(product.getBrand().getName())
                .name(product.getName())
                .slug(product.getSlug())
                .sku(product.getSku())
                .description(product.getDescription())
                .shortDescription(product.getShortDescription())
                .thumbnailUrl(product.getThumbnailUrl())
                .status(product.getStatus())
                .viewCount(product.getViewCount())
                .ratingAvg(product.getRatingAvg())
                .ratingCount(product.getRatingCount())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .variants(product.getVariants().stream()
                        .filter(v -> v.getDeletedAt() == null)
                        .map(this::mapToProductVariantResponse)
                        .toList())
                .build();
    }

    private ProductVariantResponse mapToProductVariantResponse(ProductVariant variant) {
        return ProductVariantResponse.builder()
                .id(variant.getId())
                .productId(variant.getProduct().getId())
                .sku(variant.getSku())
                .name(variant.getName())
                .price(variant.getPrice())
                .compareAtPrice(variant.getCompareAtPrice())
                .lowStockThreshold(variant.getLowStockThreshold())
                .weightGrams(variant.getWeightGrams())
                .lengthCm(variant.getLengthCm())
                .widthCm(variant.getWidthCm())
                .heightCm(variant.getHeightCm())
                .status(variant.getStatus())
                .version(variant.getVersion())
                .createdAt(variant.getCreatedAt())
                .updatedAt(variant.getUpdatedAt())
                .attributes(variant.getAttributeValues().stream()
                        .map(av -> AttributeResponse.builder()
                                .attributeId(av.getAttribute().getId())
                                .attributeName(av.getAttribute().getName())
                                .valueId(av.getId())
                                .value(av.getValue())
                                .build())
                        .toList())
                .build();
    }
}
