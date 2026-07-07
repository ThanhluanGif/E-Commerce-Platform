package com.ecommerce.ecommerceapi.controller;

import com.ecommerce.ecommerceapi.dto.ApiResponse;
import com.ecommerce.ecommerceapi.dto.DashboardStatsDTO;
import com.ecommerce.ecommerceapi.dto.RevenueChartDataDTO;
import com.ecommerce.ecommerceapi.service.AdminDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/dashboard")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    @Autowired
    private AdminDashboardService adminDashboardService;

    // 1. GET: Lấy thống kê tổng quan (Admin only)
    @GetMapping
    public ResponseEntity<ApiResponse<DashboardStatsDTO>> getStats() {
        DashboardStatsDTO stats = adminDashboardService.getStats();
        return ResponseEntity.ok(ApiResponse.success("Lấy số liệu thống kê thành công!", stats));
    }

    // 2. GET: Lấy số liệu biểu đồ doanh thu theo ngày (Admin only)
    @GetMapping("/revenue")
    public ResponseEntity<ApiResponse<List<RevenueChartDataDTO>>> getRevenueChart() {
        List<RevenueChartDataDTO> revenue = adminDashboardService.getDailyRevenue();
        return ResponseEntity.ok(ApiResponse.success("Lấy biểu đồ doanh thu thành công!", revenue));
    }
}
