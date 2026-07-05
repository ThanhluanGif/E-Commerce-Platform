package com.ecommerce.ecommerceapi.entity;

import lombok.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "shipping_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "tracking_code", length = 100)
    private String trackingCode;

    @Column(name = "shipping_provider", length = 100)
    private String shippingProvider;

    @Column(name = "shipping_fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal shippingFee;

    @Column(name = "estimated_weight", precision = 8, scale = 2)
    private BigDecimal estimatedWeight;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ShippingStatus status;

    @Column(name = "from_address", length = 500)
    private String fromAddress;

    @Column(name = "to_address", nullable = false, length = 500)
    private String toAddress;

    @Column(name = "estimated_delivery")
    private LocalDateTime estimatedDelivery;

    @Column(name = "actual_delivery")
    private LocalDateTime actualDelivery;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
