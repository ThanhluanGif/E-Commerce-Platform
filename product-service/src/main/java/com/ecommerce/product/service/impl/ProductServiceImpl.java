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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.data.jpa.domain.Specification;
import com.ecommerce.product.document.ProductDocument;
import com.ecommerce.product.mapper.ProductDocumentMapper;
import com.ecommerce.product.repository.es.ElasticsearchQueryBuilder;
import com.ecommerce.product.repository.spec.ProductSpecifications;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final AttributeRepository attributeRepository;
    private final AttributeValueRepository attributeValueRepository;
    private final ElasticsearchOperations elasticsearchOperations;

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
                // Find or create Attribute with concurrent safety
                Attribute attribute;
                try {
                    attribute = attributeRepository.findByName(attrReq.getName())
                            .orElseGet(() -> attributeRepository.saveAndFlush(
                                    Attribute.builder()
                                            .name(attrReq.getName())
                                            .isFilterable(true)
                                            .build()
                            ));
                } catch (org.springframework.dao.DataIntegrityViolationException ex) {
                    attribute = attributeRepository.findByName(attrReq.getName())
                            .orElseThrow(() -> new IllegalStateException("Failed to find or create attribute: " + attrReq.getName()));
                }

                // Find or create AttributeValue with concurrent safety
                final Attribute finalAttr = attribute;
                AttributeValue attributeValue;
                try {
                    attributeValue = attributeValueRepository
                            .findByAttributeNameAndValue(attrReq.getName(), attrReq.getValue())
                            .orElseGet(() -> attributeValueRepository.saveAndFlush(
                                    AttributeValue.builder()
                                            .attribute(finalAttr)
                                            .value(attrReq.getValue())
                                            .build()
                            ));
                } catch (org.springframework.dao.DataIntegrityViolationException ex) {
                    attributeValue = attributeValueRepository
                            .findByAttributeNameAndValue(attrReq.getName(), attrReq.getValue())
                            .orElseThrow(() -> new IllegalStateException("Failed to find or create attribute value: " + attrReq.getValue()));
                }

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

        product.getVariants().clear();
        product.getVariants().addAll(variants);

        ProductResponse response = mapToProductResponse(product);
        syncToElasticsearch(product);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "product:detail", key = "#id")
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
    @CacheEvict(value = "product:detail", key = "#id")
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
        ProductResponse response = mapToProductResponse(saved);
        syncToElasticsearch(saved);
        return response;
    }

    @Override
    @Transactional
    @CacheEvict(value = "product:detail", key = "#id")
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
        removeFromElasticsearch(id);
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

    private List<Long> getDescendantCategoryIds(Long parentId) {
        List<Category> allCategories = categoryRepository.findAllByIsActiveTrueOrderBySortOrderAsc();
        List<Long> result = new ArrayList<>();
        result.add(parentId);

        java.util.Map<Long, List<Long>> parentToChildren = allCategories.stream()
                .filter(c -> c.getParent() != null)
                .collect(java.util.stream.Collectors.groupingBy(
                        c -> c.getParent().getId(),
                        java.util.stream.Collectors.mapping(Category::getId, java.util.stream.Collectors.toList())
                ));

        collectDescendantIds(parentId, parentToChildren, result);
        return result;
    }

    private void collectDescendantIds(Long parentId, java.util.Map<Long, List<Long>> parentToChildren, List<Long> result) {
        List<Long> children = parentToChildren.get(parentId);
        if (children != null) {
            for (Long childId : children) {
                result.add(childId);
                collectDescendantIds(childId, parentToChildren, result);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(String keyword, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        List<Long> categoryIds = null;
        if (categoryId != null) {
            categoryIds = getDescendantCategoryIds(categoryId);
        }

        try {
            log.info("Searching products via Elasticsearch: keyword={}, categoryIds={}, minPrice={}, maxPrice={}", keyword, categoryIds, minPrice, maxPrice);
            NativeQuery query = ElasticsearchQueryBuilder.buildSearchQuery(keyword, categoryIds, minPrice, maxPrice, pageable);
            
            SearchHits<ProductDocument> searchHits = elasticsearchOperations.search(query, ProductDocument.class);
            
            List<ProductResponse> content = searchHits.getSearchHits().stream()
                    .map(hit -> mapToProductResponse(hit.getContent()))
                    .toList();
            
            return PageableExecutionUtils.getPage(content, pageable, searchHits::getTotalHits);
        } catch (Exception e) {
            log.warn("Elasticsearch search failed, falling back to database spec query: {}", e.getMessage(), e);
            return searchProductsFallback(keyword, categoryIds, minPrice, maxPrice, pageable);
        }
    }

    private Page<ProductResponse> searchProductsFallback(String keyword, List<Long> categoryIds, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        Specification<Product> spec = ProductSpecifications.searchProducts(keyword, categoryIds, minPrice, maxPrice);
        return productRepository.findAll(spec, pageable)
                .map(this::mapToProductResponse);
    }

    private void syncToElasticsearch(Product product) {
        if (org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive()) {
            org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                    new org.springframework.transaction.support.TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            performSyncToElasticsearch(product);
                        }
                    }
            );
        } else {
            performSyncToElasticsearch(product);
        }
    }

    private void performSyncToElasticsearch(Product product) {
        try {
            if (product.getDeletedAt() != null || product.getStatus() != ProductStatus.ACTIVE) {
                performRemoveFromElasticsearch(product.getId());
                return;
            }
            ProductDocument doc = ProductDocumentMapper.toDocument(product);
            elasticsearchOperations.save(doc);
            log.info("Successfully synced product ID {} to Elasticsearch", product.getId());
        } catch (Exception e) {
            log.error("Failed to sync product ID {} to Elasticsearch: {}", product.getId(), e.getMessage());
        }
    }

    private void removeFromElasticsearch(Long productId) {
        if (org.springframework.transaction.support.TransactionSynchronizationManager.isActualTransactionActive()) {
            org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                    new org.springframework.transaction.support.TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            performRemoveFromElasticsearch(productId);
                        }
                    }
            );
        } else {
            performRemoveFromElasticsearch(productId);
        }
    }

    private void performRemoveFromElasticsearch(Long productId) {
        try {
            elasticsearchOperations.delete(String.valueOf(productId), ProductDocument.class);
            log.info("Successfully removed product ID {} from Elasticsearch", productId);
        } catch (Exception e) {
            log.error("Failed to remove product ID {} from Elasticsearch: {}", productId, e.getMessage());
        }
    }

    private ProductResponse mapToProductResponse(ProductDocument doc) {
        return ProductResponse.builder()
                .id(doc.getId() != null ? Long.valueOf(doc.getId()) : null)
                .categoryId(doc.getCategoryId())
                .categoryName(doc.getCategoryName())
                .brandId(doc.getBrandId())
                .brandName(doc.getBrandName())
                .name(doc.getName())
                .slug(doc.getSlug())
                .sku(doc.getSku())
                .description(doc.getDescription())
                .shortDescription(doc.getShortDescription())
                .thumbnailUrl(doc.getThumbnailUrl())
                .status(ProductStatus.valueOf(doc.getStatus()))
                .createdAt(doc.getCreatedAt())
                .updatedAt(doc.getUpdatedAt())
                .variants(doc.getVariants() == null ? new ArrayList<>() : doc.getVariants().stream()
                        .map(v -> ProductVariantResponse.builder()
                                .id(v.getId())
                                .productId(doc.getId() != null ? Long.valueOf(doc.getId()) : null)
                                .sku(v.getSku())
                                .name(v.getName())
                                .price(v.getPrice())
                                .compareAtPrice(v.getCompareAtPrice())
                                .lowStockThreshold(v.getLowStockThreshold())
                                .weightGrams(v.getWeightGrams())
                                .lengthCm(v.getLengthCm())
                                .widthCm(v.getWidthCm())
                                .heightCm(v.getHeightCm())
                                .status(VariantStatus.valueOf(v.getStatus()))
                                .attributes(v.getAttributes() == null ? new ArrayList<>() : v.getAttributes().stream()
                                        .map(a -> AttributeResponse.builder()
                                                .attributeId(a.getAttributeId())
                                                .attributeName(a.getAttributeName())
                                                .valueId(a.getValueId())
                                                .value(a.getValue())
                                                .build())
                                        .toList())
                                .build())
                        .toList())
                .build();
    }
}
