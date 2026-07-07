package com.ecommerce.ecommerceapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReferralDTO {
    private Integer id;
    private String refereeUsername;
    private String refereeEmail;
    private String status;
    private boolean rewarded;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
