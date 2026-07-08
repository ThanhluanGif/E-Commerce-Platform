package com.ecommerce.order.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "warehouse_stocks", schema = "inventory")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(name = "product_variant_id", nullable = false)
    private Long productVariantId;

    @Column(name = "physical_qty", nullable = false)
    @Builder.Default
    private Integer physicalQty = 0;

    @Column(name = "reserved_qty", nullable = false)
    @Builder.Default
    private Integer reservedQty = 0;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
