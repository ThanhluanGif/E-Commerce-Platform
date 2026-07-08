package com.ecommerce.order.service.impl;

import com.ecommerce.common.exception.AppException;
import com.ecommerce.order.entity.Order;
import com.ecommerce.order.entity.PaymentTransaction;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.order.repository.PaymentTransactionRepository;
import com.ecommerce.order.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentTransactionRepository transactionRepository;

    @Value("${vnp.tmn-code}")
    private String tmnCode;

    @Value("${vnp.hash-secret}")
    private String hashSecret;

    @Value("${vnp.url}")
    private String paymentUrl;

    @Value("${vnp.return-url}")
    private String returnUrl;

    @Override
    @Transactional
    public String createPaymentUrl(Long orderId, String ipAddress) throws Exception {
        log.info("Generating VNPay redirect URL for orderId: {}, client IP: {}", orderId, ipAddress);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng!"));

        if (!"PENDING".equalsIgnoreCase(order.getStatus())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Đơn hàng đã được xử lý hoặc đã hủy!");
        }

        // 1. Create unique transaction record in system
        long txId = System.currentTimeMillis() * 1000 + ThreadLocalRandom.current().nextInt(1000);
        String transactionCode = "TXN-" + order.getOrderCode() + "-" + System.currentTimeMillis();

        PaymentTransaction transaction = PaymentTransaction.builder()
                .id(txId)
                .orderCreatedAt(order.getCreatedAt())
                .orderId(order.getId())
                .transactionCode(transactionCode)
                .paymentGateway("VNPAY")
                .amount(order.getFinalAmount())
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();
        transactionRepository.save(transaction);

        // 2. Prepare VNPay query parameters
        long amountInCents = order.getFinalAmount().multiply(BigDecimal.valueOf(100)).longValue();

        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", tmnCode);
        vnpParams.put("vnp_Amount", String.valueOf(amountInCents));
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", order.getOrderCode());
        vnpParams.put("vnp_OrderInfo", "Thanh toan don hang #" + order.getOrderCode());
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", returnUrl);
        vnpParams.put("vnp_IpAddr", ipAddress != null ? ipAddress : "127.0.0.1");

        // Format dates in Vietnam/HCM timezone
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnpCreateDate = formatter.format(cld.getTime());
        vnpParams.put("vnp_CreateDate", vnpCreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnpExpireDate = formatter.format(cld.getTime());
        vnpParams.put("vnp_ExpireDate", vnpExpireDate);

        // Sort keys alphabetically
        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (int i = 0; i < fieldNames.size(); i++) {
            String fieldName = fieldNames.get(i);
            String fieldValue = vnpParams.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                // Build hash data
                if (hashData.length() > 0) {
                    hashData.append('&');
                }
                hashData.append(fieldName).append('=').append(encode(fieldValue));

                // Build query
                if (query.length() > 0) {
                    query.append('&');
                }
                query.append(encode(fieldName)).append('=').append(encode(fieldValue));
            }
        }

        String vnpSecureHash = hmacSHA512(hashSecret, hashData.toString());
        String finalRedirectUrl = paymentUrl + "?" + query.toString() + "&vnp_SecureHash=" + vnpSecureHash;

        log.info("Successfully generated VNPay redirect URL: {}", finalRedirectUrl);
        return finalRedirectUrl;
    }

    @Override
    @Transactional
    public boolean processVNPayIPN(Map<String, String> params) {
        log.info("Received VNPay IPN Callback request params: {}", params);

        // 1. Verify Checksum signature
        if (!verifyChecksum(params)) {
            log.error("VNPay Checksum verification failed!");
            throw new AppException(HttpStatus.BAD_REQUEST, "Invalid Checksum signature");
        }

        String orderCode = params.get("vnp_TxnRef");
        String responseCode = params.get("vnp_ResponseCode");
        String transactionNo = params.get("vnp_TransactionNo");
        String amountParam = params.get("vnp_Amount");

        if (orderCode == null || responseCode == null) {
            log.error("Missing mandatory IPN parameters!");
            throw new AppException(HttpStatus.BAD_REQUEST, "Missing parameters");
        }

        // 2. Locate order in database
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Order not found"));

        // 3. Verify total amount matching
        long vnpAmount = Long.parseLong(amountParam);
        long expectedAmount = order.getFinalAmount().multiply(BigDecimal.valueOf(100)).longValue();
        if (vnpAmount != expectedAmount) {
            log.error("Payment amount mismatch! Expected: {}, Received: {}", expectedAmount, vnpAmount);
            throw new AppException(HttpStatus.BAD_REQUEST, "Amount mismatch");
        }

        // 4. Check if order has already been confirmed/paid
        if ("PAID".equalsIgnoreCase(order.getPaymentStatus()) || "CONFIRMED".equalsIgnoreCase(order.getStatus())) {
            log.info("Order {} is already paid/confirmed. Skipping status updates.", orderCode);
            // Return true because it was already successfully handled previously
            return true;
        }

        // 5. Query pending transaction or create a placeholder
        List<PaymentTransaction> txs = transactionRepository.findByOrderId(order.getId());
        PaymentTransaction transaction = txs.stream()
                .filter(t -> "PENDING".equalsIgnoreCase(t.getStatus()))
                .findFirst()
                .orElse(null);

        if (transaction == null) {
            long txId = System.currentTimeMillis() * 1000 + ThreadLocalRandom.current().nextInt(1000);
            transaction = PaymentTransaction.builder()
                    .id(txId)
                    .orderId(order.getId())
                    .orderCreatedAt(order.getCreatedAt())
                    .paymentGateway("VNPAY")
                    .amount(order.getFinalAmount())
                    .createdAt(LocalDateTime.now())
                    .build();
        }

        transaction.setTransactionCode(transactionNo != null ? transactionNo : "UNKNOWN");
        transaction.setRawResponse(params.toString());

        // 6. Handle Payment Status Success / Fail
        if ("00".equals(responseCode)) {
            log.info("Payment successful for Order Code: {}. Updating statuses to PAID and CONFIRMED.", orderCode);
            order.setPaymentStatus("PAID");
            order.setStatus("CONFIRMED");
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);

            transaction.setStatus("SUCCESS");
            transactionRepository.save(transaction);
            return true;
        } else {
            log.info("Payment failed with code {} for Order Code: {}.", responseCode, orderCode);
            order.setPaymentStatus("FAILED");
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);

            transaction.setStatus("FAILED");
            transactionRepository.save(transaction);
            return false;
        }
    }

    private boolean verifyChecksum(Map<String, String> params) {
        String vnpSecureHash = params.get("vnp_SecureHash");
        if (vnpSecureHash == null || vnpSecureHash.isEmpty()) {
            return false;
        }

        Map<String, String> hashParams = new HashMap<>(params);
        hashParams.remove("vnp_SecureHash");
        hashParams.remove("vnp_SecureHashType");

        List<String> fieldNames = new ArrayList<>(hashParams.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        for (String fieldName : fieldNames) {
            String fieldValue = hashParams.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                if (hashData.length() > 0) {
                    hashData.append('&');
                }
                hashData.append(fieldName).append('=').append(encode(fieldValue));
            }
        }

        String calculatedHash = hmacSHA512(hashSecret, hashData.toString());
        return calculatedHash.equalsIgnoreCase(vnpSecureHash);
    }

    private String encode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString()).replace("+", "%20");
        } catch (Exception e) {
            return "";
        }
    }

    private String hmacSHA512(final String key, final String data) {
        try {
            if (key == null || data == null) {
                throw new NullPointerException();
            }
            final Mac hmac512 = Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes(StandardCharsets.UTF_8);
            final SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception ex) {
            log.error("Failed to calculate HMAC-SHA512 Secure Hash", ex);
            return "";
        }
    }

    @Override
    @Transactional
    public boolean refundPayment(Long orderId, BigDecimal amount) {
        log.info("Initiating refund for Order ID: {} with amount: {}", orderId, amount);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng!"));

        List<PaymentTransaction> transactions = transactionRepository.findByOrderId(orderId);
        PaymentTransaction successTx = transactions.stream()
                .filter(t -> "SUCCESS".equalsIgnoreCase(t.getStatus()))
                .findFirst()
                .orElse(null);

        String gateway = successTx != null ? successTx.getPaymentGateway() : "COD";
        log.info("Found transaction gateway: {}. Order Payment Method: {}", gateway, order.getPaymentMethod());

        if ("VNPAY".equalsIgnoreCase(gateway) || "VNPAY".equalsIgnoreCase(order.getPaymentMethod())) {
            log.info("Simulating VNPay Refund API Call...");
            log.info("VNPay Refund Payload: [vnp_RequestId={}, vnp_Version=2.1.0, vnp_Command=refund, vnp_TmnCode={}, vnp_TransactionType=02, vnp_TxnRef={}, vnp_Amount={}, vnp_CreateBy=Admin, vnp_CreateDate={}]",
                    UUID.randomUUID().toString(),
                    tmnCode,
                    order.getOrderCode(),
                    amount.multiply(BigDecimal.valueOf(100)).longValue(),
                    new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
            
            // Create a new REFUND transaction in the database
            long txId = System.currentTimeMillis() * 1000 + ThreadLocalRandom.current().nextInt(1000);
            PaymentTransaction refundTx = PaymentTransaction.builder()
                    .id(txId)
                    .orderId(orderId)
                    .orderCreatedAt(order.getCreatedAt())
                    .transactionCode("REF-" + order.getOrderCode() + "-" + System.currentTimeMillis())
                    .paymentGateway("VNPAY")
                    .amount(amount)
                    .status("REFUNDED")
                    .createdAt(LocalDateTime.now())
                    .rawResponse("SUCCESS_MOCK_REFUND")
                    .build();
            transactionRepository.save(refundTx);

            // Update order payment status to REFUNDED
            order.setPaymentStatus("REFUNDED");
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);
            log.info("VNPay refund mock API returned status 00. Refund successful.");
            return true;
        } else if ("STRIPE".equalsIgnoreCase(gateway) || "STRIPE".equalsIgnoreCase(order.getPaymentMethod())) {
            log.info("Simulating Stripe Refund API Call...");
            log.info("Stripe Charge ID: {}, Amount: {}", successTx != null ? successTx.getTransactionCode() : "ch_stripe_mock", amount);
            
            long txId = System.currentTimeMillis() * 1000 + ThreadLocalRandom.current().nextInt(1000);
            PaymentTransaction refundTx = PaymentTransaction.builder()
                    .id(txId)
                    .orderId(orderId)
                    .orderCreatedAt(order.getCreatedAt())
                    .transactionCode("REF-" + order.getOrderCode() + "-" + System.currentTimeMillis())
                    .paymentGateway("STRIPE")
                    .amount(amount)
                    .status("REFUNDED")
                    .createdAt(LocalDateTime.now())
                    .rawResponse("SUCCESS_MOCK_STRIPE_REFUND")
                    .build();
            transactionRepository.save(refundTx);

            order.setPaymentStatus("REFUNDED");
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);
            return true;
        } else {
            // For COD or manual, we also accept it and log
            log.info("Payment method is COD/Other. Registering manual refund registration.");
            long txId = System.currentTimeMillis() * 1000 + ThreadLocalRandom.current().nextInt(1000);
            PaymentTransaction refundTx = PaymentTransaction.builder()
                    .id(txId)
                    .orderId(orderId)
                    .orderCreatedAt(order.getCreatedAt())
                    .transactionCode("REF-MANUAL-" + order.getOrderCode() + "-" + System.currentTimeMillis())
                    .paymentGateway("MANUAL")
                    .amount(amount)
                    .status("REFUNDED")
                    .createdAt(LocalDateTime.now())
                    .build();
            transactionRepository.save(refundTx);

            order.setPaymentStatus("REFUNDED");
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);
            return true;
        }
    }
}

