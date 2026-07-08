package com.ecommerce.order.controller;

import com.ecommerce.common.dto.ApiResponse;
import com.ecommerce.common.exception.AppException;
import com.ecommerce.order.dto.QcRequestDto;
import com.ecommerce.order.dto.ReturnRequestDto;
import com.ecommerce.order.dto.ReturnResponseDto;
import com.ecommerce.order.service.RmaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;

@RestController
@RequestMapping("/api/v1/returns")
@RequiredArgsConstructor
@Slf4j
public class RmaController {

    private final RmaService rmaService;

    @PostMapping
    public ResponseEntity<ApiResponse<ReturnResponseDto>> submitReturn(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody ReturnRequestDto requestDto) {
        
        log.info("Received return request from user: {} for order: {}", userId, requestDto.getOrderId());
        ReturnResponseDto response = rmaService.submitReturn(userId, requestDto);
        return ResponseEntity.ok(ApiResponse.success("Gửi yêu cầu đổi trả thành công!", response));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<ReturnResponseDto>> approveReturn(
            @RequestHeader("X-User-Roles") String rolesHeader,
            @PathVariable Long id,
            @RequestParam("orderCreatedAt") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime orderCreatedAt) {
        
        log.info("Admin approving return request ID: {}", id);
        verifyAdmin(rolesHeader);

        ReturnResponseDto response = rmaService.approveReturn(id, orderCreatedAt);
        return ResponseEntity.ok(ApiResponse.success("Phê duyệt yêu cầu đổi trả thành công! Mã vận đơn trả hàng đã được tạo.", response));
    }

    @PostMapping("/{id}/qc")
    public ResponseEntity<ApiResponse<ReturnResponseDto>> processQc(
            @RequestHeader("X-User-Roles") String rolesHeader,
            @PathVariable Long id,
            @Valid @RequestBody QcRequestDto qcRequestDto) {
        
        log.info("QC processing return request ID: {}, Pass: {}", id, qcRequestDto.isQcPassed());
        verifyAdminOrStaff(rolesHeader);

        qcRequestDto.setReturnRequestId(id);
        ReturnResponseDto response = rmaService.processQc(qcRequestDto);
        return ResponseEntity.ok(ApiResponse.success("Xác nhận hàng tại kho và hoàn tất kiểm tra chất lượng.", response));
    }

    private void verifyAdmin(String rolesHeader) {
        if (rolesHeader == null || rolesHeader.isBlank() ||
                Arrays.stream(rolesHeader.split(",")).map(String::trim).noneMatch(role -> "ROLE_ADMIN".equalsIgnoreCase(role))) {
            log.warn("Access denied: missing ROLE_ADMIN in header: {}", rolesHeader);
            throw new AppException(HttpStatus.FORBIDDEN, "Access denied: Admin role required for this operation.");
        }
    }

    private void verifyAdminOrStaff(String rolesHeader) {
        if (rolesHeader == null || rolesHeader.isBlank() ||
                Arrays.stream(rolesHeader.split(","))
                        .map(String::trim)
                        .noneMatch(role -> "ROLE_ADMIN".equalsIgnoreCase(role) || "ROLE_STAFF".equalsIgnoreCase(role))) {
            log.warn("Access denied: missing ROLE_ADMIN or ROLE_STAFF in header: {}", rolesHeader);
            throw new AppException(HttpStatus.FORBIDDEN, "Access denied: Admin or Staff role required for this operation.");
        }
    }
}
