package com.ecommerce.ecommerceapi.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // id INT (Khóa chính tự tăng)

    @Column(nullable = false)
    private Integer quantity; // quantity INT (Số lượng mua)

    @Column(name = "price_at_purchase", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceAtPurchase; // price_at_purchase DECIMAL(10,2) - Giá lúc mua

    // --- CẤU HÌNH CÁC MỐI QUAN HỆ KHÓA NGOẠI ---

    // 1. Nhiều chi tiết đơn hàng (dòng sản phẩm) thuộc về 1 Đơn hàng chính
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false) // Ánh xạ cột order_id dưới database
    private Order order;

    // 2. Nhiều chi tiết đơn hàng trỏ đến 1 Sản phẩm
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false) // Ánh xạ cột product_id dưới database
    private Product product;

    // 3. Nhiều chi tiết đơn hàng trỏ đến 1 Biến thể sản phẩm (tùy chọn)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    private ProductVariant variant;
}
