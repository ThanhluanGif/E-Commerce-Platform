package com.ecommerce.ecommerceapi.entity;

import lombok.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Entity
@Table(name = "orders")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder


public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // id INT (Khóa chính tự tăng)

    @Column(name = "order_code", length = 100, unique = true)
    private String orderCode;

    @Column(name = "total_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPrice; // total_price DECIMAL(12,2) - Chuẩn tiền tệ đơn hàng lớn

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status; // status ENUM(...) - Ánh xạ thông qua Enum ở Bước 1

    @Column(name = "shipping_address", columnDefinition = "TEXT")
    private String shippingAddress; // shipping_address TEXT

    @Column(name = "payment_method", length = 50) // Giới hạn độ dài giống VARCHAR(5...) trong ảnh
    private String paymentMethod; // payment_method VARCHAR

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // created_at TIMESTAMP - Tự động lưu thời gian tạo đơn

    // --- CẤU HÌNH MỐI QUAN HỆ KHÓA NGOẠI ---
    // Nhiều đơn hàng thuộc về một Người dùng (User)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // Tự động ánh xạ cột user_id dưới database
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems;

    // --- HÀM TỰ ĐỘNG THÊM THỜI GIAN KHI TẠO ĐƠN ---
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now(); // Tự động lấy giờ hệ thống khi người dùng bấm đặt hàng
    }
}
