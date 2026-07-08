package com.ecommerce.order.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnRequestDto {
    @NotNull(message = "Order ID cannot be null")
    private Long orderId;

    @NotNull(message = "Order creation timestamp cannot be null")
    private LocalDateTime orderCreatedAt;

    @NotEmpty(message = "Return reason cannot be empty")
    private String reason;

    @NotEmpty(message = "At least one item must be returned")
    private List<ReturnItemDto> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReturnItemDto {
        @NotNull(message = "Order item ID cannot be null")
        private Long orderItemId;

        @NotNull(message = "Quantity cannot be null")
        private Integer quantity;
    }
}
