package com.ecommerce.ecommerceapi.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartItemRequest {
    @NotNull(message = "Mã sản phẩm không được trống!")
    private Integer productId;

    @NotNull(message = "Số lượng không được trống!")
    @Min(value = 1, message = "Số lượng phải lớn hơn hoặc bằng 1!")
    private Integer quantity;
}
