package com.ecommerce.product.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ecommerce.product.dto.ProductCreateRequest;
import com.ecommerce.product.dto.ProductResponse;
import com.ecommerce.product.dto.ProductVariantCreateRequest;
import com.ecommerce.product.dto.ProductVariantResponse;
import com.ecommerce.product.entity.ProductStatus;
import com.ecommerce.product.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @Test
    void createProduct_shouldReturnCreatedProduct() throws Exception {
        ProductCreateRequest request = ProductCreateRequest.builder()
                .categoryId(10L)
                .brandId(1L)
                .name("iPhone 15 Pro")
                .slug("iphone-15-pro")
                .sku("IP15P-BASE")
                .status(ProductStatus.ACTIVE)
                .variants(List.of(
                        ProductVariantCreateRequest.builder()
                                .sku("IP15P-BLK-128")
                                .name("iPhone 15 Pro Black 128")
                                .price(new BigDecimal("28000000.00"))
                                .build()
                ))
                .build();

        ProductResponse response = ProductResponse.builder()
                .id(2001L)
                .categoryId(10L)
                .categoryName("Thiết Bị Điện Tử")
                .brandId(1L)
                .brandName("Apple")
                .name("iPhone 15 Pro")
                .slug("iphone-15-pro")
                .sku("IP15P-BASE")
                .status(ProductStatus.ACTIVE)
                .variants(List.of(
                        ProductVariantResponse.builder()
                                .id(9001L)
                                .productId(2001L)
                                .sku("IP15P-BLK-128")
                                .name("iPhone 15 Pro Black 128")
                                .price(new BigDecimal("28000000.00"))
                                .build()
                ))
                .build();

        when(productService.createProduct(any(ProductCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Product created successfully"))
                .andExpect(jsonPath("$.data.id").value(2001))
                .andExpect(jsonPath("$.data.name").value("iPhone 15 Pro"))
                .andExpect(jsonPath("$.data.variants[0].sku").value("IP15P-BLK-128"));
    }

    @Test
    void getProductById_shouldReturnProduct() throws Exception {
        ProductResponse response = ProductResponse.builder()
                .id(2001L)
                .categoryId(10L)
                .categoryName("Thiết Bị Điện Tử")
                .brandId(1L)
                .brandName("Apple")
                .name("iPhone 15 Pro")
                .slug("iphone-15-pro")
                .sku("IP15P-BASE")
                .status(ProductStatus.ACTIVE)
                .variants(List.of())
                .build();

        when(productService.getProductById(2001L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/products/2001")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Product retrieved successfully"))
                .andExpect(jsonPath("$.data.id").value(2001))
                .andExpect(jsonPath("$.data.name").value("iPhone 15 Pro"));
    }
}
