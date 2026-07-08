package com.ecommerce.notification.consumer;

import com.ecommerce.common.event.InvoiceEmailEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.Duration;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class InvoiceEmailConsumer {

    private final StringRedisTemplate redisTemplate;
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "notification.invoice.email.queue")
    public void consumeInvoiceEmail(Message message, @Header(name = AmqpHeaders.MESSAGE_ID, required = false) String messageId) throws Exception {
        log.info("Received invoice email event. Message ID from header parameter: {}", messageId);

        // Fallback to message properties
        if (messageId == null || messageId.isEmpty()) {
            messageId = message.getMessageProperties().getMessageId();
        }

        // Fallback to headers map
        if (messageId == null || messageId.isEmpty()) {
            messageId = (String) message.getMessageProperties().getHeaders().get("messageId");
        }

        // Generate fallback messageId if none exists (for compatibility)
        if (messageId == null || messageId.isEmpty()) {
            messageId = UUID.randomUUID().toString();
            log.warn("Message ID is missing. Generated a random fallback message ID: {}", messageId);
        }

        String redisKey = "msg:processed:" + messageId;
        
        // Atomic idempotency check in Redis
        Boolean isNew = redisTemplate.opsForValue().setIfAbsent(redisKey, "true", Duration.ofMinutes(10));
        
        if (Boolean.FALSE.equals(isNew)) {
            log.warn("Duplicate message detected (already processed). Skipping processing for Message ID: {}", messageId);
            return;
        }

        try {
            // Parse event
            InvoiceEmailEvent event = objectMapper.readValue(message.getBody(), InvoiceEmailEvent.class);
            log.info("Processing invoice email for Order Code: {}, Customer: {}", event.getOrderCode(), event.getCustomerEmail());

            // Simulate infrastructure/network connectivity error
            if (event.getCustomerEmail() != null && event.getCustomerEmail().endsWith("@error.com")) {
                log.warn("Simulating infrastructure network connectivity failure for email: {}", event.getCustomerEmail());
                throw new RuntimeException("Simulated connection timeout to SMTP server");
            }

            // Send HTML Email
            sendHtmlInvoiceEmail(event);
            log.info("Successfully sent invoice email for Order Code: {}", event.getOrderCode());

        } catch (Exception e) {
            log.error("Error processing message with ID: {}. Re-throwing to trigger AMQP retry.", messageId, e);
            // Delete Redis key on failure to allow reprocessing on next retry
            redisTemplate.delete(redisKey);
            throw e;
        }
    }

    private void sendHtmlInvoiceEmail(InvoiceEmailEvent event) throws Exception {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        helper.setTo(event.getCustomerEmail());
        helper.setSubject("Hóa đơn thanh toán đơn hàng #" + event.getOrderCode());

        Context context = new Context();
        context.setVariable("customerName", event.getCustomerName());
        context.setVariable("orderCode", event.getOrderCode());
        context.setVariable("totalAmount", event.getTotalAmount());
        context.setVariable("paymentMethod", event.getPaymentMethod());
        context.setVariable("paymentStatus", event.getPaymentStatus());
        context.setVariable("items", event.getItems());

        String htmlContent = templateEngine.process("invoice-email", context);
        helper.setText(htmlContent, true);

        mailSender.send(mimeMessage);
    }
}
