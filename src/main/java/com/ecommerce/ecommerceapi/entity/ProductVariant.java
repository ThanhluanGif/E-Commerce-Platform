package com.ecommerce.ecommerceapi.entity;

import lombok.*;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "product_variants")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, length = 100, unique = true)
    private String sku;

    @Column(nullable = false, length = 255)
    private String name; // e.g. "Màu Đỏ, Size M"

    @Column(precision = 10, scale = 2)
    private BigDecimal price; // Custom price for variant (optional, defaults to base product price if null)

    @Column(name = "sale_price", precision = 10, scale = 2)
    private BigDecimal salePrice; // Custom sale price for variant

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;

    @Column(name = "image_url", length = 500)
    private String imageUrl;
}
