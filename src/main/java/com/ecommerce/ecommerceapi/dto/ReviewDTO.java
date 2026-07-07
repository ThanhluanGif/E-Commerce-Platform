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
public class ReviewDTO {
    private Integer id;
    private Integer rating;
    private String comment;
    private Integer userId;
    private String username;
    private Integer productId;
    private String productName;
    private LocalDateTime createdAt;
}
