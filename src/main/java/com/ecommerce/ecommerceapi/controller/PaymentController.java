package com.ecommerce.ecommerceapi.controller;

import com.ecommerce.ecommerceapi.dto.ApiResponse;
import com.ecommerce.ecommerceapi.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Value("${app.frontend-url:http://localhost:4000}")
    private String frontendUrl;

    @PostMapping("/create-url/{orderId}")
    public ResponseEntity<ApiResponse<String>> createPaymentUrl(
            @PathVariable Integer orderId,
            HttpServletRequest request
    ) throws Exception {
        String ipAddress = request.getRemoteAddr();
        String paymentUrl = paymentService.createPaymentUrl(orderId, ipAddress);
        return ResponseEntity.ok(ApiResponse.success("Tạo link thanh toán thành công!", paymentUrl));
    }

    @GetMapping("/vnpay/ipn")
    public ResponseEntity<Map<String, String>> vnpayIPN(@RequestParam Map<String, String> params) {
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
        } catch (Exception e) {
            response.put("RspCode", "97");
            response.put("Message", "Payment Exception");
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/vnpay/return")
    public void vnpayReturn(@RequestParam Map<String, String> params, HttpServletResponse response) throws Exception {
        String responseCode = params.get("vnp_ResponseCode");
        String orderCode = params.get("vnp_TxnRef");
        
        // Redirect client to frontend result page
        response.sendRedirect(frontendUrl + "/payment-result?status=" + ("00".equals(responseCode) ? "success" : "fail") + "&orderCode=" + orderCode);
    }
}
