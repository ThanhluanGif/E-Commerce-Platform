package com.ecommerce.ecommerceapi.entity;

import lombok.*;
import jakarta.persistence.*;

@Entity
@Table(name = "warehouse_inventories", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"warehouse_id", "product_variant_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseInventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant productVariant;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "inventory_threshold", nullable = false)
    @Builder.Default
    private Integer inventoryThreshold = 5; // Cảnh báo khi tồn kho xuống dưới ngưỡng này
}
