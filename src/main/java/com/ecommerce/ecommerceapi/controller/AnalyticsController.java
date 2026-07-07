package com.ecommerce.ecommerceapi.controller;

import com.ecommerce.ecommerceapi.dto.ApiResponse;
import com.ecommerce.ecommerceapi.entity.Shop;
import com.ecommerce.ecommerceapi.entity.User;
import com.ecommerce.ecommerceapi.repository.OrderRepository;
import com.ecommerce.ecommerceapi.repository.ShopRepository;
import com.ecommerce.ecommerceapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAdminAnalytics() {
        LocalDateTime start = LocalDateTime.now().minusDays(30).with(LocalTime.MIN);
        LocalDateTime end = LocalDateTime.now().with(LocalTime.MAX);

        BigDecimal revenue = orderRepository.calculateRevenue(start, end);
        if (revenue == null) revenue = BigDecimal.ZERO;

        List<Object[]> stats = orderRepository.getOrderStatusStats();
        Map<String, Long> statusMap = new HashMap<>();
        for (Object[] row : stats) {
            statusMap.put(row[0].toString(), (Long) row[1]);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("revenue30Days", revenue);
        data.put("statusStats", statusMap);

        return ResponseEntity.ok(ApiResponse.success("Lấy số liệu thống kê Admin thành công!", data));
    }

    @GetMapping("/shop")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getShopAnalytics(Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new com.ecommerce.ecommerceapi.exception.ResourceNotFoundException("Không tìm thấy người dùng!"));
        Shop shop = shopRepository.findByUserId(user.getId())
                .orElseThrow(() -> new com.ecommerce.ecommerceapi.exception.ResourceNotFoundException("Không tìm thấy gian hàng của bạn!"));

        LocalDateTime start = LocalDateTime.now().minusDays(30).with(LocalTime.MIN);
        LocalDateTime end = LocalDateTime.now().with(LocalTime.MAX);

        BigDecimal revenue = orderRepository.calculateShopRevenue(shop.getId(), start, end);
        if (revenue == null) revenue = BigDecimal.ZERO;

        Map<String, Object> data = new HashMap<>();
        data.put("shopRevenue30Days", revenue);
        data.put("shopId", shop.getId());

        return ResponseEntity.ok(ApiResponse.success("Lấy số liệu thống kê gian hàng thành công!", data));
    }
}
