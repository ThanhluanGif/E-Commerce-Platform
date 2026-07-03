package com.ecommerce.ecommerceapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProfileUpdateRequest {
    @NotBlank(message = "Email không được trống!")
    @Email(message = "Email không đúng định dạng!")
    private String email;

    private String phone;
    private String address;
    private String avatarUrl;
}
