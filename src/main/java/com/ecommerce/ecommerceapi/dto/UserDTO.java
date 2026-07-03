package com.ecommerce.ecommerceapi.dto;

import com.ecommerce.ecommerceapi.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private Integer id;
    private String username;
    private String email;
    private UserRole role;
    private String address;
    private String phone;
    private String avatarUrl;
}
