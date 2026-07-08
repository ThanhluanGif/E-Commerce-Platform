package com.ecommerce.order.service;

import com.ecommerce.common.exception.AppException;
import com.ecommerce.order.dto.QcRequestDto;
import com.ecommerce.order.dto.ReturnRequestDto;
import com.ecommerce.order.dto.ReturnResponseDto;
import com.ecommerce.order.entity.*;
import com.ecommerce.order.repository.OrderItemRepository;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.order.repository.ReturnItemRepository;
import com.ecommerce.order.repository.ReturnRequestRepository;
import com.ecommerce.order.service.impl.RmaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RmaServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private ReturnRequestRepository returnRequestRepository;

    @Mock
    private ReturnItemRepository returnItemRepository;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private RmaServiceImpl rmaService;

    private Order completedOrder;
    private OrderItem orderItem;
    private ReturnRequestDto returnRequestDto;

    @BeforeEach
    public void setup() {
        completedOrder = Order.builder()
                .id(1001L)
                .userId(500L)
                .createdAt(LocalDateTime.now().minusDays(2))
                .status("COMPLETED")
                .paymentMethod("VNPAY")
                .paymentStatus("PAID")
                .updatedAt(LocalDateTime.now().minusDays(1)) // Completed 1 day ago
                .finalAmount(BigDecimal.valueOf(100000))
                .build();

        orderItem = OrderItem.builder()
                .id(2001L)
                .orderId(1001L)
                .orderCreatedAt(completedOrder.getCreatedAt())
                .productVariantId(3001L)
                .quantity(1)
                .unitPrice(BigDecimal.valueOf(100000))
                .discountAmount(BigDecimal.valueOf(10000)) // 10k discount
                .build();

        returnRequestDto = ReturnRequestDto.builder()
                .orderId(1001L)
                .orderCreatedAt(completedOrder.getCreatedAt())
                .reason("Sản phẩm lỗi kĩ thuật")
                .items(List.of(ReturnRequestDto.ReturnItemDto.builder()
                        .orderItemId(2001L)
                        .quantity(1)
                        .build()))
                .build();
    }

    @Test
    public void testSubmitReturn_Success() {
        when(orderRepository.findById(any(OrderId.class))).thenReturn(Optional.of(completedOrder));
        when(orderItemRepository.findById(any(OrderItemId.class))).thenReturn(Optional.of(orderItem));

        ReturnResponseDto response = rmaService.submitReturn("500", returnRequestDto);

        assertNotNull(response);
        assertEquals("PENDING", response.getStatus());
        assertEquals("PENDING", response.getRefundStatus());
        // Refund amount = 100k price - 10k discount = 90k
        assertEquals(0, BigDecimal.valueOf(90000).compareTo(response.getRefundAmount()));
        assertEquals(1, response.getItems().size());

        assertEquals(2001L, response.getItems().get(0).getOrderItemId());

        verify(returnRequestRepository, times(1)).save(any(ReturnRequest.class));
        verify(returnItemRepository, times(1)).saveAll(anyList());
    }

    @Test
    public void testSubmitReturn_OrderNotCompleted() {
        completedOrder.setStatus("PENDING");
        when(orderRepository.findById(any(OrderId.class))).thenReturn(Optional.of(completedOrder));

        AppException ex = assertThrows(AppException.class, () -> rmaService.submitReturn("500", returnRequestDto));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertTrue(ex.getMessage().contains("Only completed orders can be returned"));
    }

    @Test
    public void testSubmitReturn_OverSevenDays() {
        completedOrder.setUpdatedAt(LocalDateTime.now().minusDays(8));
        when(orderRepository.findById(any(OrderId.class))).thenReturn(Optional.of(completedOrder));

        AppException ex = assertThrows(AppException.class, () -> rmaService.submitReturn("500", returnRequestDto));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
        assertTrue(ex.getMessage().contains("within 7 days"));
    }

    @Test
    public void testApproveReturn_Success() {
        ReturnRequest pendingRequest = ReturnRequest.builder()
                .id(9001L)
                .orderId(1001L)
                .orderCreatedAt(completedOrder.getCreatedAt())
                .status("PENDING")
                .refundAmount(BigDecimal.valueOf(90000))
                .build();

        when(returnRequestRepository.findById(any(ReturnRequestId.class))).thenReturn(Optional.of(pendingRequest));
        when(returnItemRepository.findByReturnRequestId(9001L)).thenReturn(Collections.emptyList());

        ReturnResponseDto response = rmaService.approveReturn(9001L, completedOrder.getCreatedAt());

        assertNotNull(response);
        assertEquals("APPROVED", response.getStatus());
        assertNotNull(response.getReturnTrackingNumber());
        assertTrue(response.getReturnTrackingNumber().startsWith("RET-TRK-"));
        verify(returnRequestRepository, times(1)).save(pendingRequest);
    }

    @Test
    public void testProcessQc_Pass_RefundSuccess() {
        ReturnRequest approvedRequest = ReturnRequest.builder()
                .id(9001L)
                .orderId(1001L)
                .orderCreatedAt(completedOrder.getCreatedAt())
                .status("APPROVED")
                .refundAmount(BigDecimal.valueOf(90000))
                .refundStatus("PENDING")
                .build();

        ReturnItem returnItem = ReturnItem.builder()
                .id(8001L)
                .returnRequestId(9001L)
                .orderItemId(2001L)
                .quantity(1)
                .refundPrice(BigDecimal.valueOf(90000))
                .orderCreatedAt(completedOrder.getCreatedAt())
                .build();

        QcRequestDto qcRequest = QcRequestDto.builder()
                .returnRequestId(9001L)
                .orderCreatedAt(completedOrder.getCreatedAt())
                .inspectedBy(99L)
                .inspectionNotes("Hàng nguyên vẹn, đủ điều kiện.")
                .qcPassed(true)
                .items(List.of(QcRequestDto.QcItemDto.builder()
                        .orderItemId(2001L)
                        .condition("UNOPENED")
                        .build()))
                .build();

        when(returnRequestRepository.findById(any(ReturnRequestId.class))).thenReturn(Optional.of(approvedRequest));
        when(returnItemRepository.findByReturnRequestId(9001L)).thenReturn(List.of(returnItem));
        when(paymentService.refundPayment(eq(1001L), eq(BigDecimal.valueOf(90000)))).thenReturn(true);

        ReturnResponseDto response = rmaService.processQc(qcRequest);

        assertNotNull(response);
        assertEquals("REFUNDED", response.getStatus());
        assertEquals("SUCCESS", response.getRefundStatus());
        verify(returnRequestRepository, times(1)).save(approvedRequest);
        verify(returnItemRepository, times(1)).save(returnItem);
        assertEquals(99L, returnItem.getInspectedBy());
        assertEquals("UNOPENED", returnItem.getCondition());
    }

    @Test
    public void testProcessQc_Fail() {
        ReturnRequest approvedRequest = ReturnRequest.builder()
                .id(9001L)
                .orderId(1001L)
                .orderCreatedAt(completedOrder.getCreatedAt())
                .status("APPROVED")
                .refundAmount(BigDecimal.valueOf(90000))
                .refundStatus("PENDING")
                .build();

        ReturnItem returnItem = ReturnItem.builder()
                .id(8001L)
                .returnRequestId(9001L)
                .orderItemId(2001L)
                .quantity(1)
                .refundPrice(BigDecimal.valueOf(90000))
                .orderCreatedAt(completedOrder.getCreatedAt())
                .build();

        QcRequestDto qcRequest = QcRequestDto.builder()
                .returnRequestId(9001L)
                .orderCreatedAt(completedOrder.getCreatedAt())
                .inspectedBy(99L)
                .inspectionNotes("Hàng rách bao bì, trầy xước nặng.")
                .qcPassed(false)
                .items(List.of(QcRequestDto.QcItemDto.builder()
                        .orderItemId(2001L)
                        .condition("DAMAGED")
                        .build()))
                .build();

        when(returnRequestRepository.findById(any(ReturnRequestId.class))).thenReturn(Optional.of(approvedRequest));
        when(returnItemRepository.findByReturnRequestId(9001L)).thenReturn(List.of(returnItem));

        ReturnResponseDto response = rmaService.processQc(qcRequest);

        assertNotNull(response);
        assertEquals("REJECTED", response.getStatus());
        assertEquals("REJECTED", response.getRefundStatus());
        verify(returnRequestRepository, times(1)).save(approvedRequest);
        verify(paymentService, never()).refundPayment(anyLong(), any(BigDecimal.class));
    }
}
