package com.ecommerce.ecommerceapi.service;

import com.ecommerce.ecommerceapi.entity.Order;
import com.ecommerce.ecommerceapi.entity.User;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.HashMap;
import java.util.Map;

@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username:noreply@eshop.com}")
    private String fromEmail;

    @Value("${app.frontend-url:http://localhost:4000}")
    private String frontendUrl;

    @Async
    public void sendWelcomeEmail(User user) {
        if (mailSender == null) {
            System.out.println("MailSender not configured. Welcome email printing: Welcome " + user.getUsername());
            return;
        }

        Map<String, Object> variables = new HashMap<>();
        variables.put("username", user.getUsername());

        String htmlContent = generateHtmlContent("email/welcome", variables);
        sendEmail(user.getEmail(), "Chào mừng bạn đến với E-Shop!", htmlContent);
    }

    @Async
    public void sendOrderConfirmation(Order order) {
        if (mailSender == null) {
            System.out.println("MailSender not configured. Order confirmation printing: Order " + order.getOrderCode());
            return;
        }

        Map<String, Object> variables = new HashMap<>();
        variables.put("username", order.getUser().getUsername());
        variables.put("orderCode", order.getOrderCode());
        variables.put("createdAt", order.getCreatedAt());
        variables.put("shippingAddress", order.getShippingAddress());
        variables.put("paymentMethod", order.getPaymentMethod());
        variables.put("items", order.getOrderItems());
        variables.put("totalPrice", order.getTotalPrice());

        String htmlContent = generateHtmlContent("email/order-confirmation", variables);
        sendEmail(order.getUser().getEmail(), "Xác nhận đơn hàng #" + order.getOrderCode() + " tại E-Shop", htmlContent);
    }

    @Async
    public void sendPasswordResetEmail(User user, String resetToken) {
        if (mailSender == null) {
            System.out.println("MailSender not configured. Password reset printing: Reset Token " + resetToken);
            return;
        }

        Map<String, Object> variables = new HashMap<>();
        variables.put("username", user.getUsername());
        variables.put("resetUrl", frontendUrl + "/reset-password?token=" + resetToken);

        String htmlContent = generateHtmlContent("email/password-reset", variables);
        sendEmail(user.getEmail(), "Yêu cầu khôi phục mật khẩu tài khoản E-Shop", htmlContent);
    }

    private String generateHtmlContent(String templateName, Map<String, Object> variables) {
        Context context = new Context();
        context.setVariables(variables);
        return templateEngine.process(templateName, context);
    }

    private void sendEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Gửi email thất bại đến " + to + ": " + e.getMessage());
        }
    }
}
