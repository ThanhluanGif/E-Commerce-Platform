package com.ecommerce.ecommerceapi.entity;


public enum OrderStatus {
    PENDING,    // Chờ xử lý / Chờ duyệt
    SHIPPING,   // Đang giao hàng
    DELIVERED,  // Đã giao hàng thành công
    CANCELLED   // Đã hủy đơn
}
