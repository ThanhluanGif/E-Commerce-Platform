package com.ecommerce.order.controller;

import com.ecommerce.common.exception.AppException;
import com.ecommerce.common.exception.GlobalExceptionHandler;
import com.ecommerce.order.exception.OrderExceptionHandler;
import com.ecommerce.order.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
@Import({GlobalExceptionHandler.class, OrderExceptionHandler.class})
public class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @Test
    void testCreatePaymentUrlSuccess() throws Exception {
        String fakeRedirectUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?foo=bar";
        when(paymentService.createPaymentUrl(eq(123L), anyString())).thenReturn(fakeRedirectUrl);

        mockMvc.perform(post("/api/payments/create-url/123")
                        .remoteAddress("127.0.0.1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(fakeRedirectUrl));

        verify(paymentService, times(1)).createPaymentUrl(eq(123L), anyString());
    }

    @Test
    void testCreatePaymentUrlOrderNotFound() throws Exception {
        when(paymentService.createPaymentUrl(eq(999L), anyString()))
                .thenThrow(new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng!"));

        mockMvc.perform(post("/api/payments/create-url/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Không tìm thấy đơn hàng!"));
    }

    @Test
    void testVNPayIPNSuccess() throws Exception {
        when(paymentService.processVNPayIPN(anyMap())).thenReturn(true);

        mockMvc.perform(get("/api/v1/payments/vnpay-ipn")
                        .param("vnp_TxnRef", "ORD-123")
                        .param("vnp_ResponseCode", "00")
                        .param("vnp_SecureHash", "123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.RspCode").value("00"))
                .andExpect(jsonPath("$.Message").value("Confirm Success"));
    }

    @Test
    void testVNPayIPNChecksumInvalid() throws Exception {
        when(paymentService.processVNPayIPN(anyMap()))
                .thenThrow(new AppException(HttpStatus.BAD_REQUEST, "Invalid Checksum signature"));

        mockMvc.perform(get("/api/v1/payments/vnpay-ipn")
                        .param("vnp_TxnRef", "ORD-123")
                        .param("vnp_ResponseCode", "00")
                        .param("vnp_SecureHash", "wrong"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.RspCode").value("97"))
                .andExpect(jsonPath("$.Message").value("Invalid Checksum"));
    }

    @Test
    void testVNPayIPNOrderNotFound() throws Exception {
        when(paymentService.processVNPayIPN(anyMap()))
                .thenThrow(new AppException(HttpStatus.NOT_FOUND, "Order not found"));

        mockMvc.perform(get("/api/v1/payments/vnpay-ipn")
                        .param("vnp_TxnRef", "ORD-not-found")
                        .param("vnp_ResponseCode", "00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.RspCode").value("01"))
                .andExpect(jsonPath("$.Message").value("Order not found"));
    }

    @Test
    void testVNPayReturnRedirect() throws Exception {
        mockMvc.perform(get("/api/v1/payments/vnpay-return")
                        .param("vnp_ResponseCode", "00")
                        .param("vnp_TxnRef", "ORD-123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/payment-result?status=success&orderCode=ORD-123"));
    }
}
