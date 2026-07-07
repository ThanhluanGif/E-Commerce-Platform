package com.ecommerce.ecommerceapi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderRequest {
    @NotBlank(message = "Địa chỉ nhận hàng không được để trống!")
    private String shippingAddress;

    @NotBlank(message = "Phương thức thanh toán không được để trống!")
    private String paymentMethod;

    private BigDecimal discountAmount;

    private Integer pointsUsed;
}
