package com.ecommerce.ecommerceapi.dto;

import com.ecommerce.ecommerceapi.entity.ReturnStatus;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReturnRequestDTO {
    private Integer id;
    private Integer orderId;
    private String orderCode;
    private Integer userId;
    private String username;
    private String reason;
    private String imagesUrl;
    private ReturnStatus status;
    private String sellerNote;
    private String adminNote;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
