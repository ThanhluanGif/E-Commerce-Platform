package com.ecommerce.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceEmailEvent {
    private String messageId;
    private Long orderId;
    private String orderCode;
    private String customerEmail;
    private String customerName;
    private BigDecimal totalAmount;
    private String paymentMethod;
    private String paymentStatus;
    private List<InvoiceItem> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvoiceItem {
        private String productName;
        private Integer quantity;
        private BigDecimal price;
    }
}
