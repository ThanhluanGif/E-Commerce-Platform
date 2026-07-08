package com.ecommerce.order.controller;

import com.ecommerce.common.dto.ApiResponse;
import com.ecommerce.common.exception.AppException;
import com.ecommerce.order.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @PostMapping({"/api/payments/create-url/{orderId}", "/api/v1/payments/create-url/{orderId}"})
    public ResponseEntity<ApiResponse<String>> createPaymentUrl(
            @PathVariable Long orderId,
            HttpServletRequest request
    ) throws Exception {
        String ipAddress = request.getRemoteAddr();
        String paymentUrl = paymentService.createPaymentUrl(orderId, ipAddress);
        return ResponseEntity.ok(ApiResponse.success("Tạo link thanh toán thành công!", paymentUrl));
    }

    @GetMapping("/api/v1/payments/vnpay-ipn")
    public ResponseEntity<Map<String, String>> vnpayIPN(@RequestParam Map<String, String> params) {
        log.info("Incoming VNPay IPN params: {}", params);
        Map<String, String> response = new HashMap<>();
        try {
            boolean success = paymentService.processVNPayIPN(params);
            if (success) {
                response.put("RspCode", "00");
                response.put("Message", "Confirm Success");
            } else {
                response.put("RspCode", "99");
                response.put("Message", "Confirm Error");
            }
        } catch (AppException e) {
            String message = e.getMessage();
            log.warn("VNPay IPN processing AppError: {}", message);
            if ("Invalid Checksum signature".equalsIgnoreCase(message)) {
                response.put("RspCode", "97");
                response.put("Message", "Invalid Checksum");
            } else if ("Order not found".equalsIgnoreCase(message)) {
                response.put("RspCode", "01");
                response.put("Message", "Order not found");
            } else if ("Order already confirmed".equalsIgnoreCase(message)) {
                response.put("RspCode", "02");
                response.put("Message", "Order already confirmed");
            } else if ("Amount mismatch".equalsIgnoreCase(message)) {
                response.put("RspCode", "04");
                response.put("Message", "Invalid Amount");
            } else {
                response.put("RspCode", "99");
                response.put("Message", "Confirm Error: " + message);
            }
        } catch (Exception e) {
            log.error("VNPay IPN processing error", e);
            response.put("RspCode", "99");
            response.put("Message", "Unknown Error");
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/v1/payments/vnpay-return")
    public void vnpayReturn(@RequestParam Map<String, String> params, HttpServletResponse response) throws Exception {
        log.info("Incoming VNPay return redirect params: {}", params);
        String responseCode = params.get("vnp_ResponseCode");
        String orderCode = params.get("vnp_TxnRef");
        
        // Redirect browser to frontend result page
        String redirectUrl = frontendUrl + "/payment-result?status=" + 
                ("00".equals(responseCode) ? "success" : "fail") + 
                "&orderCode=" + orderCode;
        
        log.info("Redirecting client browser to: {}", redirectUrl);
        response.sendRedirect(redirectUrl);
    }
}
