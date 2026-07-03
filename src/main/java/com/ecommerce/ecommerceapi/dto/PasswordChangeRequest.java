package com.ecommerce.ecommerceapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordChangeRequest {
    @NotBlank(message = "Mật khẩu cũ không được trống!")
    private String oldPassword;

    @NotBlank(message = "Mật khẩu mới không được trống!")
    @Size(min = 6, message = "Mật khẩu mới phải có tối thiểu 6 ký tự!")
    private String newPassword;
}
