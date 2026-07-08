package com.ecommerce.order.service;

import com.ecommerce.common.exception.AppException;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.PaymentTransaction;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.order.repository.PaymentTransactionRepository;
import com.ecommerce.order.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentTransactionRepository transactionRepository;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(paymentService, "tmnCode", "2QX7C6YK");
        ReflectionTestUtils.setField(paymentService, "hashSecret", "HJDFYUREIYOWQDNBVCHFYTRUEIORPWQA");
        ReflectionTestUtils.setField(paymentService, "paymentUrl", "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html");
        ReflectionTestUtils.setField(paymentService, "returnUrl", "http://localhost:8080/api/v1/payments/vnpay-return");
    }

    @Test
    void testCreatePaymentUrlSuccess() throws Exception {
        Order order = Order.builder()
                .id(100L)
                .orderCode("ORD-100")
                .finalAmount(BigDecimal.valueOf(500000))
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        String paymentUrl = paymentService.createPaymentUrl(100L, "127.0.0.1");

        assertNotNull(paymentUrl);
        assertTrue(paymentUrl.contains("vnp_TmnCode=2QX7C6YK"));
        assertTrue(paymentUrl.contains("vnp_Amount=50000000")); // amount * 100
        assertTrue(paymentUrl.contains("vnp_TxnRef=ORD-100"));
        assertTrue(paymentUrl.contains("vnp_SecureHash="));

        // Verify transaction is stored in database
        ArgumentCaptor<PaymentTransaction> txCaptor = ArgumentCaptor.forClass(PaymentTransaction.class);
        verify(transactionRepository, times(1)).save(txCaptor.capture());
        PaymentTransaction savedTx = txCaptor.getValue();
        assertEquals(100L, savedTx.getOrderId());
        assertEquals("PENDING", savedTx.getStatus());
        assertEquals(BigDecimal.valueOf(500000), savedTx.getAmount());
    }

    @Test
    void testProcessVNPayIPNSuccess() {
        Order order = Order.builder()
                .id(200L)
                .orderCode("ORD-200")
                .finalAmount(BigDecimal.valueOf(100000))
                .status("PENDING")
                .paymentStatus("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        when(orderRepository.findByOrderCode("ORD-200")).thenReturn(Optional.of(order));
        when(transactionRepository.findByOrderId(200L)).thenReturn(Collections.emptyList());

        // Construct mock VNPay callback parameters
        Map<String, String> params = new HashMap<>();
        params.put("vnp_TmnCode", "2QX7C6YK");
        params.put("vnp_Amount", "10000000");
        params.put("vnp_TxnRef", "ORD-200");
        params.put("vnp_ResponseCode", "00");
        params.put("vnp_TransactionNo", "12345678");

        // Manually compute secure hash using secret key and parameter ordering
        String hashData = "vnp_Amount=10000000&vnp_ResponseCode=00&vnp_TmnCode=2QX7C6YK&vnp_TransactionNo=12345678&vnp_TxnRef=ORD-200";
        // Calculate hash
        String secureHash = calculateHMAC512("HJDFYUREIYOWQDNBVCHFYTRUEIORPWQA", hashData);
        params.put("vnp_SecureHash", secureHash);

        boolean result = paymentService.processVNPayIPN(params);

        assertTrue(result);
        assertEquals("CONFIRMED", order.getStatus());
        assertEquals("PAID", order.getPaymentStatus());

        ArgumentCaptor<PaymentTransaction> txCaptor = ArgumentCaptor.forClass(PaymentTransaction.class);
        verify(transactionRepository, times(1)).save(txCaptor.capture());
        PaymentTransaction savedTx = txCaptor.getValue();
        assertEquals(200L, savedTx.getOrderId());
        assertEquals("SUCCESS", savedTx.getStatus());
        assertEquals("12345678", savedTx.getTransactionCode());
    }

    @Test
    void testProcessVNPayIPNFailed() {
        Order order = Order.builder()
                .id(300L)
                .orderCode("ORD-300")
                .finalAmount(BigDecimal.valueOf(150000))
                .status("PENDING")
                .paymentStatus("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        when(orderRepository.findByOrderCode("ORD-300")).thenReturn(Optional.of(order));
        when(transactionRepository.findByOrderId(300L)).thenReturn(Collections.emptyList());

        Map<String, String> params = new HashMap<>();
        params.put("vnp_TmnCode", "2QX7C6YK");
        params.put("vnp_Amount", "15000000");
        params.put("vnp_TxnRef", "ORD-300");
        params.put("vnp_ResponseCode", "24"); // User cancelled transaction
        params.put("vnp_TransactionNo", "87654321");

        String hashData = "vnp_Amount=15000000&vnp_ResponseCode=24&vnp_TmnCode=2QX7C6YK&vnp_TransactionNo=87654321&vnp_TxnRef=ORD-300";
        String secureHash = calculateHMAC512("HJDFYUREIYOWQDNBVCHFYTRUEIORPWQA", hashData);
        params.put("vnp_SecureHash", secureHash);

        boolean result = paymentService.processVNPayIPN(params);

        assertFalse(result);
        assertEquals("FAILED", order.getPaymentStatus());
        assertNotEquals("CONFIRMED", order.getStatus()); // Order should not be confirmed on failure

        ArgumentCaptor<PaymentTransaction> txCaptor = ArgumentCaptor.forClass(PaymentTransaction.class);
        verify(transactionRepository, times(1)).save(txCaptor.capture());
        PaymentTransaction savedTx = txCaptor.getValue();
        assertEquals(300L, savedTx.getOrderId());
        assertEquals("FAILED", savedTx.getStatus());
        assertEquals("87654321", savedTx.getTransactionCode());
    }

    private String calculateHMAC512(String key, String data) {
        try {
            javax.crypto.Mac hmac512 = javax.crypto.Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            javax.crypto.spec.SecretKeySpec secretKey = new javax.crypto.spec.SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);
            byte[] result = hmac512.doFinal(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception ex) {
            return "";
        }
    }
}
