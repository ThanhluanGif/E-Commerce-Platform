package com.ecommerce.ecommerceapi.controller;

import com.ecommerce.ecommerceapi.dto.ApiResponse;
import com.ecommerce.ecommerceapi.dto.OrderDTO;
import com.ecommerce.ecommerceapi.dto.OrderItemDTO;
import com.ecommerce.ecommerceapi.dto.OrderRequest;
import com.ecommerce.ecommerceapi.entity.Order;
import com.ecommerce.ecommerceapi.entity.OrderStatus;
import com.ecommerce.ecommerceapi.repository.UserRepository;
import com.ecommerce.ecommerceapi.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    private Integer getUserId(Principal principal) {
        if (principal == null) {
            throw new com.ecommerce.ecommerceapi.exception.BadRequestException("Yêu cầu cần được xác thực!");
        }
        return userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new com.ecommerce.ecommerceapi.exception.ResourceNotFoundException("Không tìm thấy người dùng!"))
                .getId();
    }

    // 1. POST: Đặt hàng (Checkout)
    @PostMapping
    public ResponseEntity<ApiResponse<OrderDTO>> checkout(
            @Valid @RequestBody OrderRequest request,
            Principal principal
    ) {
        Integer userId = getUserId(principal);
        Order savedOrder = orderService.createOrder(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Đặt hàng thành công!", convertToDTO(savedOrder)));
    }

    // 2. GET: Lịch sử đơn hàng của người dùng hiện tại
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderDTO>>> getMyOrders(Principal principal) {
        Integer userId = getUserId(principal);
        List<OrderDTO> orders = orderService.getOrdersForUser(userId).stream()
                .map(this::convertToDTO)
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách đơn hàng thành công!", orders));
    }

    // 3. GET: Chi tiết một đơn hàng theo ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderDTO>> getOrderDetails(
            @PathVariable Integer id,
            Principal principal
    ) {
        Integer userId = getUserId(principal);
        Order order = orderService.getOrderById(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết đơn hàng thành công!", convertToDTO(order)));
    }

    // 4. PUT: Hủy đơn hàng (Chỉ được hủy khi trạng thái là PENDING)
    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderDTO>> cancelOrder(
            @PathVariable Integer id,
            Principal principal
    ) {
        Integer userId = getUserId(principal);
        Order cancelledOrder = orderService.cancelOrder(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Hủy đơn hàng thành công!", convertToDTO(cancelledOrder)));
    }

    // 5. GET: Quản lý xem toàn bộ đơn hàng (Chỉ dành cho ADMIN)
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<OrderDTO>>> getAllOrders(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<OrderDTO> orders = orderService.getAllOrders(pageable)
                .map(this::convertToDTO);
        return ResponseEntity.ok(ApiResponse.success("Lấy toàn bộ danh sách đơn hàng thành công!", orders));
    }

    // 6. PUT: Cập nhật trạng thái đơn hàng (Chỉ dành cho ADMIN)
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OrderDTO>> updateStatus(
            @PathVariable Integer id,
            @RequestParam OrderStatus status
    ) {
        Order updatedOrder = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái đơn hàng thành công!", convertToDTO(updatedOrder)));
    }

    // 7. POST: Thanh toán đơn hàng giả lập (MOMO/VNPAY)
    @PostMapping("/{id}/pay")
    public ResponseEntity<ApiResponse<OrderDTO>> payOrder(@PathVariable Integer id) {
        Order paidOrder = orderService.payOrder(id);
        return ResponseEntity.ok(ApiResponse.success("Thanh toán đơn hàng thành công!", convertToDTO(paidOrder)));
    }

    private OrderDTO convertToDTO(Order order) {
        List<OrderItemDTO> itemDTOs = null;
        if (order.getOrderItems() != null) {
            itemDTOs = order.getOrderItems().stream()
                    .map(item -> OrderItemDTO.builder()
                            .id(item.getId())
                            .productId(item.getProduct().getId())
                            .productName(item.getProduct().getName())
                            .productImageUrl(item.getVariant() != null && item.getVariant().getImageUrl() != null && !item.getVariant().getImageUrl().trim().isEmpty()
                                    ? item.getVariant().getImageUrl()
                                    : item.getProduct().getImageUrl())
                            .quantity(item.getQuantity())
                            .priceAtPurchase(item.getPriceAtPurchase())
                            .variantId(item.getVariant() != null ? item.getVariant().getId() : null)
                            .variantName(item.getVariant() != null ? item.getVariant().getName() : null)
                            .build())
                    .toList();
        }

        return OrderDTO.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .shippingAddress(order.getShippingAddress())
                .paymentMethod(order.getPaymentMethod())
                .createdAt(order.getCreatedAt())
                .userId(order.getUser().getId())
                .username(order.getUser().getUsername())
                .items(itemDTOs)
                .build();
    }
}
