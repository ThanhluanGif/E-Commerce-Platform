package com.ecommerce.product.service;

import com.ecommerce.product.dto.*;
import com.ecommerce.product.entity.*;
import com.ecommerce.product.repository.*;
import com.ecommerce.product.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductVariantRepository productVariantRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private BrandRepository brandRepository;
    @Mock
    private AttributeRepository attributeRepository;
    @Mock
    private AttributeValueRepository attributeValueRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void createProduct_shouldResolveDynamicAttributesAndSaveProductCorrectly() {
        // Arrange
        Category category = Category.builder().id(10L).name("Thiết Bị Điện Tử").build();
        Brand brand = Brand.builder().id(1L).name("Apple").build();
        
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(category));
        when(brandRepository.findById(1L)).thenReturn(Optional.of(brand));
        when(productRepository.existsBySkuAndDeletedAtIsNull("IP15P-BASE")).thenReturn(false);
        when(productRepository.existsBySlugAndDeletedAtIsNull("iphone-15-pro")).thenReturn(false);
        
        Product savedProduct = Product.builder()
                .id(2001L)
                .category(category)
                .brand(brand)
                .name("iPhone 15 Pro")
                .slug("iphone-15-pro")
                .sku("IP15P-BASE")
                .status(ProductStatus.ACTIVE)
                .variants(new ArrayList<>())
                .build();
        
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        // Mock Attribute resolution
        Attribute colorAttr = Attribute.builder().id(1L).name("Màu sắc").build();
        Attribute capacityAttr = Attribute.builder().id(2L).name("Dung lượng").build();
        
        when(attributeRepository.findByName("Màu sắc")).thenReturn(Optional.empty());
        when(attributeRepository.save(argThat(a -> a != null && "Màu sắc".equals(a.getName())))).thenReturn(colorAttr);
        when(attributeRepository.findByName("Dung lượng")).thenReturn(Optional.empty());
        when(attributeRepository.save(argThat(a -> a != null && "Dung lượng".equals(a.getName())))).thenReturn(capacityAttr);

        // Mock AttributeValue resolution
        AttributeValue blackVal = AttributeValue.builder().id(50L).attribute(colorAttr).value("Black").build();
        AttributeValue sizeVal = AttributeValue.builder().id(52L).attribute(capacityAttr).value("128GB").build();
        
        when(attributeValueRepository.findByAttributeNameAndValue("Màu sắc", "Black")).thenReturn(Optional.empty());
        when(attributeValueRepository.save(argThat(av -> av != null && "Black".equals(av.getValue())))).thenReturn(blackVal);
        when(attributeValueRepository.findByAttributeNameAndValue("Dung lượng", "128GB")).thenReturn(Optional.empty());
        when(attributeValueRepository.save(argThat(av -> av != null && "128GB".equals(av.getValue())))).thenReturn(sizeVal);

        // Mock Variant save
        ProductVariant variant = ProductVariant.builder()
                .id(9001L)
                .product(savedProduct)
                .sku("IP15P-BLK-128")
                .name("iPhone 15 Pro - Black - 128GB")
                .price(new BigDecimal("28000000.00"))
                .status(VariantStatus.ACTIVE)
                .attributeValues(List.of(blackVal, sizeVal))
                .build();
        
        when(productVariantRepository.existsBySkuAndDeletedAtIsNull("IP15P-BLK-128")).thenReturn(false);
        when(productVariantRepository.save(any(ProductVariant.class))).thenReturn(variant);

        ProductVariantCreateRequest variantReq = ProductVariantCreateRequest.builder()
                .sku("IP15P-BLK-128")
                .name("iPhone 15 Pro - Black - 128GB")
                .price(new BigDecimal("28000000.00"))
                .status(VariantStatus.ACTIVE)
                .attributes(List.of(
                        new AttributeMappingRequest("Màu sắc", "Black"),
                        new AttributeMappingRequest("Dung lượng", "128GB")
                ))
                .build();

        ProductCreateRequest request = ProductCreateRequest.builder()
                .categoryId(10L)
                .brandId(1L)
                .name("iPhone 15 Pro")
                .slug("iphone-15-pro")
                .sku("IP15P-BASE")
                .status(ProductStatus.ACTIVE)
                .variants(List.of(variantReq))
                .build();

        // Act
        ProductResponse response = productService.createProduct(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(2001L);
        assertThat(response.getCategoryName()).isEqualTo("Thiết Bị Điện Tử");
        assertThat(response.getBrandName()).isEqualTo("Apple");
        assertThat(response.getVariants()).hasSize(1);
        
        ProductVariantResponse variantRes = response.getVariants().get(0);
        assertThat(variantRes.getSku()).isEqualTo("IP15P-BLK-128");
        assertThat(variantRes.getAttributes()).hasSize(2);
        
        AttributeResponse colorAttrRes = variantRes.getAttributes().stream()
                .filter(a -> a.getAttributeName().equals("Màu sắc"))
                .findFirst().orElseThrow();
        assertThat(colorAttrRes.getValue()).isEqualTo("Black");
        assertThat(colorAttrRes.getAttributeId()).isEqualTo(1L);
        assertThat(colorAttrRes.getValueId()).isEqualTo(50L);
    }

    @Test
    void getVariantById_shouldReturnVariantDetailsWithMappedAttributes() {
        // Arrange
        Category category = Category.builder().id(10L).name("Thiết Bị Điện Tử").build();
        Brand brand = Brand.builder().id(1L).name("Apple").build();
        Product product = Product.builder().id(2001L).category(category).brand(brand).name("iPhone 15 Pro").build();
        
        Attribute colorAttr = Attribute.builder().id(1L).name("Màu sắc").build();
        AttributeValue blackVal = AttributeValue.builder().id(50L).attribute(colorAttr).value("Black").build();
        
        ProductVariant variant = ProductVariant.builder()
                .id(9001L)
                .product(product)
                .sku("IP15P-BLK-128")
                .name("iPhone 15 Pro - Black - 128GB")
                .price(new BigDecimal("28000000.00"))
                .status(VariantStatus.ACTIVE)
                .attributeValues(List.of(blackVal))
                .build();
        
        when(productVariantRepository.findByIdAndDeletedAtIsNull(9001L)).thenReturn(Optional.of(variant));
        
        // Act
        ProductVariantResponse response = productService.getVariantById(9001L);
        
        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(9001L);
        assertThat(response.getProductId()).isEqualTo(2001L);
        assertThat(response.getSku()).isEqualTo("IP15P-BLK-128");
        assertThat(response.getAttributes()).hasSize(1);
        
        AttributeResponse attrRes = response.getAttributes().get(0);
        assertThat(attrRes.getAttributeId()).isEqualTo(1L);
        assertThat(attrRes.getAttributeName()).isEqualTo("Màu sắc");
        assertThat(attrRes.getValueId()).isEqualTo(50L);
        assertThat(attrRes.getValue()).isEqualTo("Black");
    }
}
