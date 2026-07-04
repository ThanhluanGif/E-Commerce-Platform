package com.ecommerce.ecommerceapi.entity;

import lombok.*;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "flash_sale_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlashSaleItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flash_sale_id", nullable = false)
    private FlashSale flashSale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "sale_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal salePrice;

    @Column(name = "sale_quantity", nullable = false)
    private Integer saleQuantity;

    @Column(name = "sold_count", nullable = false)
    @Builder.Default
    private Integer soldCount = 0;
}
