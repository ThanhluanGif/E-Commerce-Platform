package com.ecommerce.ecommerceapi.event;

import com.ecommerce.ecommerceapi.entity.Order;
import com.ecommerce.ecommerceapi.entity.NotificationType;
import com.ecommerce.ecommerceapi.entity.OrderStatus;
import com.ecommerce.ecommerceapi.entity.OrderStatusHistory;
import com.ecommerce.ecommerceapi.repository.OrderStatusHistoryRepository;
import com.ecommerce.ecommerceapi.service.EmailService;
import com.ecommerce.ecommerceapi.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class NotificationEventListener {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private OrderStatusHistoryRepository orderStatusHistoryRepository;

    @EventListener
    public void handleOrderStatusChanged(OrderStatusChangedEvent event) {
        Order order = event.getOrder();
        if (order == null || order.getUser() == null) return;

        // Lưu lịch sử trạng thái đơn hàng
        try {
            OrderStatusHistory history = OrderStatusHistory.builder()
                    .order(order)
                    .status(order.getStatus())
                    .description(translateStatusDescription(order.getStatus()))
                    .updatedBy("SYSTEM")
                    .timestamp(LocalDateTime.now())
                    .build();
            orderStatusHistoryRepository.save(history);
        } catch (Exception e) {
            System.err.println("Lỗi khi ghi lịch sử đơn hàng: " + e.getMessage());
        }

        // Nếu đơn hàng vừa tạo (PENDING), gửi email xác nhận mua hàng
        if (order.getStatus() == OrderStatus.PENDING) {
            try {
                emailService.sendOrderConfirmation(order);
            } catch (Exception e) {
                System.err.println("Lỗi khi gửi email xác nhận đơn hàng: " + e.getMessage());
            }
        }

        String orderCode = order.getOrderCode();
        String title = "Cập nhật đơn hàng " + orderCode;
        String message = "Đơn hàng " + orderCode + " của bạn đã chuyển sang trạng thái: " + translateStatus(order.getStatus().name());
        String actionUrl = "/orders/" + order.getId();

        notificationService.createNotification(
                order.getUser().getId(),
                title,
                message,
                null,
                actionUrl,
                NotificationType.ORDER_UPDATE
        );
    }

    private String translateStatusDescription(OrderStatus status) {
        switch (status) {
            case PENDING:
                return "Đơn hàng đã được tạo thành công và đang chờ xác nhận.";
            case SHIPPING:
                return "Người bán đang chuẩn bị hàng và giao cho đơn vị vận chuyển.";
            case DELIVERED:
                return "Đơn hàng đã được giao thành công đến bạn.";
            case CANCELLED:
                return "Đơn hàng đã bị hủy.";
            default:
                return "Đơn hàng được cập nhật trạng thái.";
        }
    }

    private String translateStatus(String statusName) {
        switch (statusName) {
            case "PENDING":
                return "Chờ xác nhận";
            case "SHIPPING":
                return "Đang giao hàng";
            case "DELIVERED":
                return "Đã giao hàng";
            case "CANCELLED":
                return "Đã hủy";
            default:
                return statusName;
        }
    }
}
