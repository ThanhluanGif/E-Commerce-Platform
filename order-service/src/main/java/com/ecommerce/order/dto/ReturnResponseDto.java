package com.ecommerce.order.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnResponseDto {
    private Long id;
    private Long orderId;
    private String reason;
    private String status;
    private BigDecimal refundAmount;
    private String refundStatus;
    private String returnTrackingNumber;
    private LocalDateTime createdAt;
    private LocalDateTime orderCreatedAt;
    private List<ReturnItemResponseDto> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReturnItemResponseDto {
        private Long id;
        private Long orderItemId;
        private Integer quantity;
        private BigDecimal refundPrice;
        private String condition;
        private Long inspectedBy;
        private String inspectionNotes;
    }
}
