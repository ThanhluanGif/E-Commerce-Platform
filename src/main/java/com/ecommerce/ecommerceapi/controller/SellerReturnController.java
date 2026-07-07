package com.ecommerce.ecommerceapi.controller;

import com.ecommerce.ecommerceapi.dto.ApiResponse;
import com.ecommerce.ecommerceapi.dto.ReturnRequestDTO;
import com.ecommerce.ecommerceapi.entity.ReturnRequest;
import com.ecommerce.ecommerceapi.entity.User;
import com.ecommerce.ecommerceapi.repository.UserRepository;
import com.ecommerce.ecommerceapi.service.ReturnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/seller/returns")
@PreAuthorize("hasRole('SELLER')")
public class SellerReturnController {

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

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReturnRequestDTO>>> getShopReturns(Principal principal) {
        Integer sellerId = getUserId(principal);
        if (sellerId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Chưa đăng nhập!"));
        }

        List<ReturnRequestDTO> list = returnService.getReturnsForSeller(sellerId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách yêu cầu trả hàng của shop thành công!", list));
    }

    @PutMapping("/{id}/respond")
    public ResponseEntity<ApiResponse<ReturnRequestDTO>> respondToReturn(
            Principal principal,
            @PathVariable Integer id,
            @RequestParam boolean approved,
            @RequestParam(required = false) String note
    ) {
        Integer sellerId = getUserId(principal);
        if (sellerId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Chưa đăng nhập!"));
        }

        ReturnRequest request = returnService.sellerRespond(id, sellerId, approved, note);
        return ResponseEntity.ok(ApiResponse.success("Xử lý yêu cầu trả hàng thành công!", convertToDTO(request)));
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
