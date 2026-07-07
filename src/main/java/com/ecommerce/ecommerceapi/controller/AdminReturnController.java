package com.ecommerce.ecommerceapi.controller;

import com.ecommerce.ecommerceapi.dto.ApiResponse;
import com.ecommerce.ecommerceapi.dto.ReturnRequestDTO;
import com.ecommerce.ecommerceapi.entity.ReturnRequest;
import com.ecommerce.ecommerceapi.service.ReturnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/returns")
@PreAuthorize("hasRole('ADMIN')")
public class AdminReturnController {

    @Autowired
    private ReturnService returnService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReturnRequestDTO>>> getAllReturns() {
        List<ReturnRequestDTO> list = returnService.getAllReturnsForAdmin().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Lấy toàn bộ danh sách hoàn hàng thành công!", list));
    }

    @PutMapping("/{id}/resolve")
    public ResponseEntity<ApiResponse<ReturnRequestDTO>> resolveReturn(
            @PathVariable Integer id,
            @RequestParam boolean refund,
            @RequestParam(required = false) String note
    ) {
        ReturnRequest request = returnService.adminResolve(id, refund, note);
        return ResponseEntity.ok(ApiResponse.success("Xử lý phê duyệt hoàn tiền thành công!", convertToDTO(request)));
    }

    private ReturnRequestDTO convertToDTO(ReturnRequest request) {
        return ReturnRequestDTO.builder()
                .id(request.getId())
                .orderId(request.getOrder().getId())
                .orderCode(request.getOrder().getOrderCode())
                .userId(request.getUser().getId())
                .username(request.getUser().getUsername())
                .reason(request.getReason())
                .imagesUrl(request.getImagesUrl())
                .status(request.getStatus())
                .sellerNote(request.getSellerNote())
                .adminNote(request.getAdminNote())
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .build();
    }
}
