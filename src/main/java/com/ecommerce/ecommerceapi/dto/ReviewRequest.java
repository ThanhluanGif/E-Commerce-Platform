package com.ecommerce.ecommerceapi.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewRequest {
    @NotNull(message = "Đánh giá sao không được trống!")
    @Min(value = 1, message = "Đánh giá sao tối thiểu là 1!")
    @Max(value = 5, message = "Đánh giá sao tối đa là 5!")
    private Integer rating;

    @NotBlank(message = "Nội dung nhận xét không được trống!")
    private String comment;
}
