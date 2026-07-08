package com.ecommerce.order.controller;

import com.ecommerce.common.exception.GlobalExceptionHandler;
import com.ecommerce.order.dto.CheckoutRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.exception.OrderExceptionHandler;
import com.ecommerce.order.service.CheckoutService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CheckoutController.class)
@Import({GlobalExceptionHandler.class, OrderExceptionHandler.class})
public class CheckoutControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CheckoutService checkoutService;

    @Test
    void testCheckoutSuccess() throws Exception {
        CheckoutRequest request = CheckoutRequest.builder()
                .recipientName("Alice")
                .recipientPhone("0123456789")
                .shippingAddress("123 Street")
                .paymentMethod("COD")
                .build();

        OrderResponse response = OrderResponse.builder()
                .orderId(999L)
                .orderCode("ORD-20260708-999999")
                .totalAmount(BigDecimal.valueOf(200.0))
                .shippingFee(BigDecimal.valueOf(30.0))
                .discountAmount(BigDecimal.ZERO)
                .finalAmount(BigDecimal.valueOf(230.0))
                .status("PENDING")
                .paymentStatus("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        when(checkoutService.checkout("user123", request)).thenReturn(response);

        mockMvc.perform(post("/api/v1/orders/checkout")
                        .header("X-User-Id", "user123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Order placed successfully"))
                .andExpect(jsonPath("$.data.orderCode").value("ORD-20260708-999999"));

        verify(checkoutService, times(1)).checkout("user123", request);
    }

    @Test
    void testCheckoutValidationError() throws Exception {
        CheckoutRequest request = CheckoutRequest.builder()
                .recipientName("")
                .recipientPhone("")
                .shippingAddress("")
                .paymentMethod("")
                .build();

        mockMvc.perform(post("/api/v1/orders/checkout")
                        .header("X-User-Id", "user123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"));
    }
}
