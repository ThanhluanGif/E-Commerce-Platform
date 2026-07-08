package com.ecommerce.order.service;

import com.ecommerce.order.dto.QcRequestDto;
import com.ecommerce.order.dto.ReturnRequestDto;
import com.ecommerce.order.dto.ReturnResponseDto;

import java.time.LocalDateTime;

public interface RmaService {
    ReturnResponseDto submitReturn(String userId, ReturnRequestDto request);
    ReturnResponseDto approveReturn(Long returnId, LocalDateTime orderCreatedAt);
    ReturnResponseDto processQc(QcRequestDto request);
}
