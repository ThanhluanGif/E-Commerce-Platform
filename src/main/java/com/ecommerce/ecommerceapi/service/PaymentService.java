package com.ecommerce.ecommerceapi.service;

import com.ecommerce.ecommerceapi.entity.*;
import com.ecommerce.ecommerceapi.exception.BadRequestException;
import com.ecommerce.ecommerceapi.exception.ResourceNotFoundException;
import com.ecommerce.ecommerceapi.payment.PaymentGateway;
import com.ecommerce.ecommerceapi.repository.OrderRepository;
import com.ecommerce.ecommerceapi.repository.PaymentTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class PaymentService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentTransactionRepository transactionRepository;

    @Autowired
    @Qualifier("vnpay")
    private PaymentGateway vnpayGateway;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private OrderService orderService;

    public String createPaymentUrl(Integer orderId, String ipAddress) throws Exception {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng!"));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException("Đơn hàng đã được xử lý hoặc đã hủy!");
        }

        // Tạo hoặc cập nhật giao dịch thanh toán trong hệ thống
        String transactionCode = "TXN-" + order.getOrderCode() + "-" + System.currentTimeMillis();
        
        PaymentTransaction transaction = PaymentTransaction.builder()
                .order(order)
                .transactionCode(transactionCode)
                .amount(order.getTotalPrice())
                .paymentMethod(PaymentMethod.VNPAY)
                .status(PaymentStatus.PENDING)
                .ipAddress(ipAddress)
                .build();

        transactionRepository.save(transaction);

        // Tạo Payment URL từ cổng VNPay
        return vnpayGateway.createPaymentUrl(order, ipAddress);
    }

    public boolean processVNPayIPN(Map<String, String> params) {
        // 1. Kiểm tra checksum
        if (!vnpayGateway.verifyChecksum(params)) {
            System.err.println("VNPay Checksum verification failed!");
            return false;
        }

        String orderCode = params.get("vnp_TxnRef");
        String responseCode = params.get("vnp_ResponseCode");
        String transactionNo = params.get("vnp_TransactionNo");
        String amountParam = params.get("vnp_Amount");

        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với mã: " + orderCode));

        // Tìm transaction PENDING gần nhất của order
        Optional<PaymentTransaction> optTxn = transactionRepository.findByOrderId(order.getId())
                .stream()
                .filter(t -> t.getStatus() == PaymentStatus.PENDING)
                .findFirst();

        PaymentTransaction transaction;
        if (optTxn.isPresent()) {
            transaction = optTxn.get();
        } else {
            transaction = PaymentTransaction.builder()
                    .order(order)
                    .amount(order.getTotalPrice())
                    .paymentMethod(PaymentMethod.VNPAY)
                    .status(PaymentStatus.PENDING)
                    .build();
        }

        transaction.setTransactionCode(transactionNo);
        transaction.setGatewayResponse(params.toString());

        if ("00".equals(responseCode)) {
            // Thanh toán thành công
            transaction.setStatus(PaymentStatus.SUCCESS);
            transaction.setPaidAt(LocalDateTime.now());
            transactionRepository.save(transaction);

            // Cập nhật trạng thái đơn hàng sang SHIPPING (hoặc trạng thái tương ứng sau thanh toán)
            orderService.updateOrderStatus(order.getId(), OrderStatus.SHIPPING);

            // Gửi thông báo
            notificationService.createNotification(
                    order.getUser().getId(),
                    "Thanh toán thành công",
                    "Đơn hàng #" + order.getOrderCode() + " đã được thanh toán thành công qua VNPay.",
                    null,
                    "/orders/" + order.getId(),
                    NotificationType.ORDER_UPDATE
            );
            return true;
        } else {
            // Thanh toán thất bại
            transaction.setStatus(PaymentStatus.FAILED);
            transactionRepository.save(transaction);

            notificationService.createNotification(
                    order.getUser().getId(),
                    "Thanh toán thất bại",
                    "Giao dịch thanh toán cho đơn hàng #" + order.getOrderCode() + " không thành công.",
                    null,
                    "/orders/" + order.getId(),
                    NotificationType.ORDER_UPDATE
            );
            return false;
        }
    }
}
