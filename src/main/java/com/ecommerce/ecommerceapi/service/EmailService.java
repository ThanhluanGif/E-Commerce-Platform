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

    @Async
    public void sendFlashSaleReminderEmail(User user, com.ecommerce.ecommerceapi.entity.FlashSale flashSale) {
        if (mailSender == null) {
            System.out.println("MailSender not configured. Flash sale reminder email printing: Flash Sale " + flashSale.getName() + " for " + user.getUsername());
            return;
        }

        Map<String, Object> variables = new HashMap<>();
        variables.put("username", user.getUsername());
        variables.put("saleName", flashSale.getName());
        variables.put("startTime", flashSale.getStartTime().toString());
        variables.put("flashSaleUrl", frontendUrl + "/flash-sale");

        String htmlContent = generateHtmlContent("email/flashsale-reminder", variables);
        sendEmail(user.getEmail(), "⚡ Nhắc nhở: Flash Sale '" + flashSale.getName() + "' sắp diễn ra tại E-Shop!", htmlContent);
    }

    @Async
    public void sendAbandonedCartEmail(User user, String voucherCode, String discountDesc) {
        if (mailSender == null) {
            System.out.println("MailSender not configured. Abandoned cart email printing: Voucher " + voucherCode + " for " + user.getUsername());
            return;
        }

        Map<String, Object> variables = new HashMap<>();
        variables.put("username", user.getUsername());
        variables.put("voucherCode", voucherCode);
        variables.put("voucherDiscount", discountDesc);
        variables.put("cartUrl", frontendUrl + "/cart");

        String htmlContent = generateHtmlContent("email/cart-recovery", variables);
        sendEmail(user.getEmail(), "🛒 Bạn quên sản phẩm trong giỏ hàng kìa! Nhận ngay ưu đãi giảm giá!", htmlContent);
    }

    @Async
    public void sendLowStockAlertEmail(String toEmail, String variantName, String sku, String warehouseName, int quantity) {
        if (mailSender == null) {
            System.out.println("MailSender not configured. Low stock alert: " + variantName + " (" + sku + ") in " + warehouseName + " is at " + quantity);
            return;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("⚠️ CẢNH BÁO: Tồn kho xuống dưới mức tối thiểu!");
            helper.setText("Sản phẩm biến thể: " + variantName + " (SKU: " + sku + ") tại kho: " + warehouseName + " hiện chỉ còn: " + quantity + " sản phẩm. Vui lòng nhập hàng thêm!", false);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Gửi email cảnh báo thất bại đến " + toEmail + ": " + e.getMessage());
        }
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
