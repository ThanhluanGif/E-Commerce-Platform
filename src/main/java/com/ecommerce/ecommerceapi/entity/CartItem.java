package com.ecommerce.ecommerceapi.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Table(name = "cart_items")
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Integer quantity;

    // --- CẤU HÌNH CÁC MỐI QUAN HỆ KHÓA NGOẠI ---

    // 1. Nhiều dòng cart_item thuộc về 1 User

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // tự ánh xạ cột user_id dưới database
    private User user;
    // 2. Nhiều dòng cart_item trỏ đến 1 Product
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false) // tự ánh xạ cột product_id dưới database
    private Product product;

    // 3. Nhiều dòng cart_item có thể trỏ đến 1 ProductVariant
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    private ProductVariant variant;

    @Column(name = "updated_at")
    private java.time.LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = java.time.LocalDateTime.now();
    }
}
