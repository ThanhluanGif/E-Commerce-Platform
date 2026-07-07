package com.ecommerce.product.controller;

import com.ecommerce.product.dto.AttributeResponse;
import com.ecommerce.product.dto.ProductVariantResponse;
import com.ecommerce.product.entity.VariantStatus;
import com.ecommerce.product.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductVariantController.class)
class ProductVariantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Test
    void getVariantById_shouldReturnVariant() throws Exception {
        ProductVariantResponse response = ProductVariantResponse.builder()
                .id(9001L)
                .productId(2001L)
                .sku("IP15P-BLK-128")
                .name("iPhone 15 Pro - Black - 128GB")
                .price(new BigDecimal("28000000.00"))
                .status(VariantStatus.ACTIVE)
                .attributes(List.of(
                        AttributeResponse.builder()
                                .attributeId(1L)
                                .attributeName("Màu sắc")
                                .valueId(50L)
                                .value("Black")
                                .build()
                ))
                .build();

        when(productService.getVariantById(9001L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/variants/9001")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Variant retrieved successfully"))
                .andExpect(jsonPath("$.data.id").value(9001))
                .andExpect(jsonPath("$.data.sku").value("IP15P-BLK-128"))
                .andExpect(jsonPath("$.data.attributes[0].attributeName").value("Màu sắc"))
                .andExpect(jsonPath("$.data.attributes[0].value").value("Black"));
    }
}
