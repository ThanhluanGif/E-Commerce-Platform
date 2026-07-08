package com.ecommerce.notification.controller;

import com.ecommerce.common.dto.ApiResponse;
import com.ecommerce.common.event.InvoiceEmailEvent;
import com.ecommerce.notification.config.NotificationRabbitConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class TestNotificationController {

    private final RabbitTemplate rabbitTemplate;

    @PostMapping("/send-test-invoice")
    public ResponseEntity<ApiResponse<String>> sendTestInvoice(
            @RequestParam String email,
            @RequestParam(required = false) String msgId,
            @RequestParam(required = false, defaultValue = "1000000") BigDecimal amount
    ) {
        String messageId = (msgId != null && !msgId.isEmpty()) ? msgId : UUID.randomUUID().toString();

        InvoiceEmailEvent event = InvoiceEmailEvent.builder()
                .messageId(messageId)
                .orderId(12345L)
                .orderCode("ORD-" + System.currentTimeMillis())
                .customerEmail(email)
                .customerName("Khách Hàng Thử Nghiệm")
                .totalAmount(amount)
                .paymentMethod("VNPAY")
                .paymentStatus("PAID")
                .items(List.of(
                        InvoiceEmailEvent.InvoiceItem.builder()
                                .productName("Laptop Asus ROG Strix G15")
                                .quantity(1)
                                .price(amount.multiply(BigDecimal.valueOf(0.7)))
                                .build(),
                        InvoiceEmailEvent.InvoiceItem.builder()
                                .productName("Chuột chơi game Logitech G502")
                                .quantity(1)
                                .price(amount.multiply(BigDecimal.valueOf(0.3)))
                                .build()
                ))
                .build();

        // Publish to RabbitMQ exchange with messageId in header properties
        rabbitTemplate.convertAndSend(
                NotificationRabbitConfig.NOTIFICATION_EXCHANGE,
                NotificationRabbitConfig.INVOICE_EMAIL_ROUTING_KEY,
                event,
                message -> {
                    message.getMessageProperties().setMessageId(messageId);
                    message.getMessageProperties().setHeader("messageId", messageId);
                    return message;
                }
        );

        return ResponseEntity.ok(ApiResponse.success("Đã gửi message test gửi hóa đơn qua RabbitMQ với MessageID: " + messageId, messageId));
    }
}
