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
public class ShopDTO {
    private Integer id;
    private Integer userId;
    private String username;
    private String name;
    private String slug;
    private String description;
    private String logoUrl;
    private String bannerUrl;
    private Double rating;
    private Integer reviewCount;
    private Boolean verified;
    private Boolean active;
    private LocalDateTime joinDate;
    private Integer followerCount;
}
