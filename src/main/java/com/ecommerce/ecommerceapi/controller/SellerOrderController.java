package com.ecommerce.ecommerceapi.controller;

import com.ecommerce.ecommerceapi.dto.ApiResponse;
import com.ecommerce.ecommerceapi.dto.OrderDTO;
import com.ecommerce.ecommerceapi.dto.OrderItemDTO;
import com.ecommerce.ecommerceapi.entity.Order;
import com.ecommerce.ecommerceapi.entity.OrderStatus;
import com.ecommerce.ecommerceapi.entity.Shop;
import com.ecommerce.ecommerceapi.entity.User;
import com.ecommerce.ecommerceapi.exception.BadRequestException;
import com.ecommerce.ecommerceapi.repository.OrderRepository;
import com.ecommerce.ecommerceapi.repository.UserRepository;
import com.ecommerce.ecommerceapi.service.OrderService;
import com.ecommerce.ecommerceapi.service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/seller")
public class SellerOrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ShopService shopService;

    @Autowired
    private UserRepository userRepository;

    private Integer getUserId(Principal principal) {
        if (principal == null) return null;
        return userRepository.findByUsername(principal.getName())
                .map(User::getId)
                .orElse(null);
    }

    private Shop getSellerShop(Principal principal) {
        Integer userId = getUserId(principal);
        if (userId == null) {
            throw new BadRequestException("Chưa đăng nhập!");
        }
        return shopService.getShopByUserId(userId);
    }

    // 1. GET: Lấy danh sách đơn hàng có sản phẩm của shop tôi
    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<Page<OrderDTO>>> getMyOrders(
            Principal principal,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Shop shop = getSellerShop(principal);
        Page<OrderDTO> orders = orderRepository.findByShopId(shop.getId(), pageable)
                .map(this::convertToDTO);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách đơn hàng thành công!", orders));
    }

    // 2. PUT: Xác nhận chuẩn bị đơn hàng (Chuyển sang SHIPPING hoặc cập nhật trạng thái)
    @PutMapping("/orders/{id}/confirm")
    public ResponseEntity<ApiResponse<OrderDTO>> confirmOrder(Principal principal, @PathVariable Integer id) {
        Shop shop = getSellerShop(principal);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy đơn hàng!"));

        boolean belongsToShop = order.getOrderItems().stream()
                .anyMatch(oi -> oi.getProduct().getShop() != null && oi.getProduct().getShop().getId().equals(shop.getId()));

        if (!belongsToShop) {
            throw new BadRequestException("Đơn hàng này không thuộc quyền quản lý của shop bạn!");
        }

        order.setStatus(OrderStatus.SHIPPING);
        Order savedOrder = orderRepository.save(order);
        return ResponseEntity.ok(ApiResponse.success("Xác nhận đang giao hàng thành công!", convertToDTO(savedOrder)));
    }

    // 3. PUT: Đánh dấu đang giao đơn hàng
    @PutMapping("/orders/{id}/ship")
    public ResponseEntity<ApiResponse<OrderDTO>> shipOrder(Principal principal, @PathVariable Integer id) {
        return confirmOrder(principal, id); // Tái sử dụng logic xác nhận/đang giao
    }

    // 4. GET: Thống kê doanh thu, đơn hàng, sản phẩm của shop
    @GetMapping("/analytics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAnalytics(Principal principal) {
        Shop shop = getSellerShop(principal);
        List<Order> orders = orderRepository.findByShopIdOrderByCreatedAtDesc(shop.getId());

        long totalOrders = orders.size();
        long completedOrders = orders.stream().filter(o -> o.getStatus() == OrderStatus.DELIVERED).count();
        long pendingOrders = orders.stream().filter(o -> o.getStatus() == OrderStatus.PENDING).count();

        // Calculate revenue from this shop's items only!
        BigDecimal shopRevenue = BigDecimal.ZERO;
        long totalProductsSold = 0;

        for (Order order : orders) {
            if (order.getStatus() == OrderStatus.DELIVERED) {
                for (var item : order.getOrderItems()) {
                    if (item.getProduct().getShop() != null && item.getProduct().getShop().getId().equals(shop.getId())) {
                        BigDecimal itemTotal = item.getPriceAtPurchase().multiply(BigDecimal.valueOf(item.getQuantity()));
                        shopRevenue = shopRevenue.add(itemTotal);
                        totalProductsSold += item.getQuantity();
                    }
                }
            }
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalOrders", totalOrders);
        stats.put("completedOrders", completedOrders);
        stats.put("pendingOrders", pendingOrders);
        stats.put("totalRevenue", shopRevenue);
        stats.put("totalProductsSold", totalProductsSold);

        return ResponseEntity.ok(ApiResponse.success("Lấy số liệu thống kê thành công!", stats));
    }

    private OrderDTO convertToDTO(Order order) {
        List<OrderItemDTO> itemDTOs = order.getOrderItems().stream()
                .map(item -> OrderItemDTO.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .productImageUrl(item.getVariant() != null && item.getVariant().getImageUrl() != null && !item.getVariant().getImageUrl().trim().isEmpty()
                                ? item.getVariant().getImageUrl()
                                : item.getProduct().getImageUrl())
                        .priceAtPurchase(item.getPriceAtPurchase())
                        .quantity(item.getQuantity())
                        .variantId(item.getVariant() != null ? item.getVariant().getId() : null)
                        .variantName(item.getVariant() != null ? item.getVariant().getName() : null)
                        .build())
                .toList();

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
