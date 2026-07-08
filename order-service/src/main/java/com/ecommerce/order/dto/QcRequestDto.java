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
public class QcRequestDto {
    @NotNull(message = "Order creation timestamp cannot be null")
    private LocalDateTime orderCreatedAt;

    @NotNull(message = "Return request ID cannot be null")
    private Long returnRequestId;

    @NotNull(message = "Inspected by user ID cannot be null")
    private Long inspectedBy;


    private String inspectionNotes;

    private boolean qcPassed;

    @NotEmpty(message = "At least one inspection item details required")
    private List<QcItemDto> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QcItemDto {
        @NotNull(message = "Order item ID cannot be null")
        private Long orderItemId;

        @NotNull(message = "Condition cannot be null")
        private String condition; // UNOPENED, OPENED_GOOD, DAMAGED
    }
}
