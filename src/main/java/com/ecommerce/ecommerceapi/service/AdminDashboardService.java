package com.ecommerce.ecommerceapi.service;

import com.ecommerce.ecommerceapi.dto.DashboardStatsDTO;
import com.ecommerce.ecommerceapi.dto.RevenueChartDataDTO;
import com.ecommerce.ecommerceapi.entity.Order;
import com.ecommerce.ecommerceapi.entity.OrderStatus;
import com.ecommerce.ecommerceapi.repository.OrderRepository;
import com.ecommerce.ecommerceapi.repository.ProductRepository;
import com.ecommerce.ecommerceapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminDashboardService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    public DashboardStatsDTO getStats() {
        List<Order> deliveredOrders = orderRepository.findByStatus(OrderStatus.DELIVERED);

        BigDecimal totalRevenue = deliveredOrders.stream()
                .map(Order::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalOrders = orderRepository.count();
        long totalUsers = userRepository.count();
        long totalProducts = productRepository.count();

        return DashboardStatsDTO.builder()
                .totalRevenue(totalRevenue)
                .totalOrders(totalOrders)
                .totalUsers(totalUsers)
                .totalProducts(totalProducts)
                .build();
    }

    public List<RevenueChartDataDTO> getDailyRevenue() {
        List<Order> deliveredOrders = orderRepository.findByStatus(OrderStatus.DELIVERED);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Group by formatted date and sum total price
        Map<String, BigDecimal> dailyMap = deliveredOrders.stream()
                .filter(o -> o.getCreatedAt() != null)
                .collect(Collectors.groupingBy(
                        o -> o.getCreatedAt().format(formatter),
                        TreeMap::new, // Keep sorted by date ASC
                        Collectors.reducing(BigDecimal.ZERO, Order::getTotalPrice, BigDecimal::add)
                ));

        List<RevenueChartDataDTO> chartData = new ArrayList<>();
        dailyMap.forEach((date, revenue) -> chartData.add(
                RevenueChartDataDTO.builder()
                        .date(date)
                        .revenue(revenue)
                        .build()
        ));

        return chartData;
    }
}
