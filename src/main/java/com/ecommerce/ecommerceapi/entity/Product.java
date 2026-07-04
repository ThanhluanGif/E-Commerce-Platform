package com.ecommerce.ecommerceapi.entity;

import lombok.*;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 255, unique = true)
    private String slug;

    @Column(columnDefinition = "TEXT" )
    private String description;

    @Column(nullable = false,precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false,name = "stock_quantity")
    private Integer stockQuantity;

    @Column(name = "image_url", length = 500) // Khớp với VARCHAR(50...) trong sơ đồ của bạn
    private String imageUrl;

    // --- CẤU HÌNH MỐI QUAN HỆ KHÓA NGOẠI ---
    // Nhiều sản phẩm thuộc về một danh mục (Category)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false) // Tự động ánh xạ cột category_id dưới database
    private Category category;

    // Nhiều sản phẩm thuộc về một gian hàng (Shop)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id")
    private Shop shop;

    @Column(name = "sale_price", precision = 10, scale = 2)
    private BigDecimal salePrice;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images;
}
