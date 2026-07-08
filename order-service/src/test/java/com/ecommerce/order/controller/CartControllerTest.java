package com.ecommerce.order.controller;

import com.ecommerce.common.exception.GlobalExceptionHandler;
import com.ecommerce.order.exception.OrderExceptionHandler;
import com.ecommerce.order.dto.CartItemRequest;
import com.ecommerce.order.dto.CartItemResponse;
import com.ecommerce.order.dto.ProductVariantResponse;
import com.ecommerce.order.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
@Import({GlobalExceptionHandler.class, OrderExceptionHandler.class})
public class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CartService cartService;

    @Test
    void testAddToCartSuccess() throws Exception {
        CartItemRequest request = CartItemRequest.builder()
                .variantId(456L)
                .quantity(2)
                .build();

        doNothing().when(cartService).addToCart("user123", 456L, 2);

        mockMvc.perform(post("/api/v1/orders/cart/add")
                        .header("X-User-Id", "user123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Product added to cart successfully"));

        verify(cartService, times(1)).addToCart("user123", 456L, 2);
    }

    @Test
    void testAddToCartValidationError() throws Exception {
        CartItemRequest request = CartItemRequest.builder()
                .variantId(null)
                .quantity(0)
                .build();

        mockMvc.perform(post("/api/v1/orders/cart/add")
                        .header("X-User-Id", "user123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }

    @Test
    void testAddToCartMissingUserHeader() throws Exception {
        CartItemRequest request = CartItemRequest.builder()
                .variantId(456L)
                .quantity(2)
                .build();

        mockMvc.perform(post("/api/v1/orders/cart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetCartSuccess() throws Exception {
        List<CartItemResponse> cartItems = Collections.singletonList(
                CartItemResponse.builder()
                        .variantId(456L)
                        .quantity(2)
                        .variant(new ProductVariantResponse())
                        .build()
        );

        when(cartService.getCart("user123")).thenReturn(cartItems);

        mockMvc.perform(get("/api/v1/orders/cart/get")
                        .header("X-User-Id", "user123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Cart retrieved successfully"))
                .andExpect(jsonPath("$.data[0].variantId").value(456L))
                .andExpect(jsonPath("$.data[0].quantity").value(2));

        verify(cartService, times(1)).getCart("user123");
    }

    @Test
    void testRemoveFromCartSuccess() throws Exception {
        doNothing().when(cartService).removeFromCart("user123", 456L);

        mockMvc.perform(delete("/api/v1/orders/cart/remove/456")
                        .header("X-User-Id", "user123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Product removed from cart successfully"));

        verify(cartService, times(1)).removeFromCart("user123", 456L);
    }
}
