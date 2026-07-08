package com.ecommerce.order.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "return_items", schema = "orders")
@IdClass(ReturnItemId.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "return_request_id", nullable = false)
    private Long returnRequestId;

    @Column(name = "order_item_id", nullable = false)
    private Long orderItemId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "refund_price", nullable = false)
    private BigDecimal refundPrice;

    @Column(length = 50)
    private String condition; // UNOPENED, OPENED_GOOD, DAMAGED

    @Column(name = "inspected_by")
    private Long inspectedBy;

    @Column(name = "inspection_notes")
    private String inspectionNotes;

    @Id
    @Column(name = "order_created_at", nullable = false)
    private LocalDateTime orderCreatedAt;
}
