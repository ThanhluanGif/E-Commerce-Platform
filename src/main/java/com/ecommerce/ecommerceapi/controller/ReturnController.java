package com.ecommerce.ecommerceapi.controller;

import com.ecommerce.ecommerceapi.dto.ApiResponse;
import com.ecommerce.ecommerceapi.dto.ReturnRequestCreateDTO;
import com.ecommerce.ecommerceapi.dto.ReturnRequestDTO;
import com.ecommerce.ecommerceapi.entity.ReturnRequest;
import com.ecommerce.ecommerceapi.entity.User;
import com.ecommerce.ecommerceapi.repository.UserRepository;
import com.ecommerce.ecommerceapi.service.ReturnService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/returns")
public class ReturnController {

    @Autowired
    private ReturnService returnService;

    @Autowired
    private UserRepository userRepository;

    private Integer getUserId(Principal principal) {
        if (principal == null) return null;
        return userRepository.findByUsername(principal.getName())
                .map(User::getId)
                .orElse(null);
    }

    @PostMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<ReturnRequestDTO>> createReturnRequest(
            Principal principal,
            @PathVariable Integer orderId,
            @Valid @RequestBody ReturnRequestCreateDTO dto
    ) {
        Integer userId = getUserId(principal);
        if (userId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Chưa đăng nhập!"));
        }

        ReturnRequest request = returnService.createReturnRequest(userId, orderId, dto);
        return ResponseEntity.ok(ApiResponse.success("Yêu cầu hoàn trả đã gửi thành công!", convertToDTO(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReturnRequestDTO>>> getMyReturns(Principal principal) {
        Integer userId = getUserId(principal);
        if (userId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Chưa đăng nhập!"));
        }

        List<ReturnRequestDTO> list = returnService.getReturnsForUser(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách yêu cầu hoàn trả thành công!", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReturnRequestDTO>> getReturnDetail(Principal principal, @PathVariable Integer id) {
        Integer userId = getUserId(principal);
        if (userId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Chưa đăng nhập!"));
        }

        ReturnRequest request = returnService.getReturnById(id);
        if (!request.getUser().getId().equals(userId)) {
            return ResponseEntity.status(403).body(ApiResponse.error("Bạn không có quyền xem yêu cầu hoàn trả này!"));
        }

        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết hoàn hàng thành công!", convertToDTO(request)));
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
