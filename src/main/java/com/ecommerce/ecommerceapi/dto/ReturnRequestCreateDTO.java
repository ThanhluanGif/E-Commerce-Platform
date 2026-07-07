package com.ecommerce.ecommerceapi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReturnRequestCreateDTO {
    @NotBlank(message = "Lý do hoàn hàng không được để trống!")
    private String reason;
    private String imagesUrl;
}
