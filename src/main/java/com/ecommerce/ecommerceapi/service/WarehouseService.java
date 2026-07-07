package com.ecommerce.ecommerceapi.service;

import com.ecommerce.ecommerceapi.entity.*;
import com.ecommerce.ecommerceapi.repository.WarehouseInventoryRepository;
import com.ecommerce.ecommerceapi.repository.WarehouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class WarehouseService {

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private WarehouseInventoryRepository warehouseInventoryRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private NotificationService notificationService;

    /**
     * Thuật toán chọn kho tối ưu dựa trên Địa chỉ giao hàng và Tồn kho.
     * 1. Tìm các kho ở cùng Tỉnh/Thành phố của khách hàng.
     * 2. Nếu có nhiều kho (hoặc không có kho cùng TP), tính toán khoảng cách Haversine.
     * 3. Ưu tiên kho gần nhất có ĐỦ HÀNG cho tất cả sản phẩm biến thể trong đơn.
     */
    public Warehouse selectOptimalWarehouse(String shippingAddress, List<OrderItem> items) {
        List<Warehouse> warehouses = warehouseRepository.findAll();
        if (warehouses.isEmpty()) {
            return null; // Không có kho nào được định nghĩa
        }

        // Tọa độ giả lập dựa trên địa chỉ giao hàng của khách
        double userLat = 21.0285; // Mặc định Hà Nội
        double userLng = 105.8542;
        String addressLower = shippingAddress.toLowerCase();

        if (addressLower.contains("hồ chí minh") || addressLower.contains("tphcm") || addressLower.contains("sài gòn") || addressLower.contains("hcm")) {
            userLat = 10.8231;
            userLng = 106.6297;
        } else if (addressLower.contains("đà nẵng") || addressLower.contains("danang")) {
            userLat = 16.0544;
            userLng = 108.2022;
        } else if (addressLower.contains("cần thơ")) {
            userLat = 10.0452;
            userLng = 105.7469;
        } else if (addressLower.contains("hải phòng")) {
            userLat = 20.8449;
            userLng = 106.6881;
        }

        Warehouse bestWarehouse = null;
        double minDistance = Double.MAX_VALUE;

        for (Warehouse wh : warehouses) {
            // Kiểm tra xem kho này có đủ hàng cho tất cả các items không
            boolean hasAllStock = true;
            for (OrderItem item : items) {
                if (item.getVariant() != null) {
                    Optional<WarehouseInventory> invOpt = warehouseInventoryRepository
                            .findByWarehouseIdAndProductVariantId(wh.getId(), item.getVariant().getId());
                    if (invOpt.isEmpty() || invOpt.get().getQuantity() < item.getQuantity()) {
                        hasAllStock = false;
                        break;
                    }
                }
            }

            // Tính khoảng cách Haversine nếu kho có đủ hàng
            if (hasAllStock) {
                double distance = calculateHaversineDistance(
                        userLat, userLng, 
                        wh.getLatitude() != null ? wh.getLatitude() : 21.0285, 
                        wh.getLongitude() != null ? wh.getLongitude() : 105.8542
                );

                // Ưu tiên trùng khớp Thành Phố trực tiếp trước
                boolean cityMatches = false;
                if (wh.getCity() != null && addressLower.contains(wh.getCity().toLowerCase())) {
                    cityMatches = true;
                }

                // Nếu trùng thành phố, giảm khoảng cách tính toán ảo để ưu tiên hàng đầu
                double score = cityMatches ? (distance / 10.0) : distance;

                if (score < minDistance) {
                    minDistance = score;
                    bestWarehouse = wh;
                }
            }
        }

        // Fallback: nếu không kho nào đủ hàng cho TOÀN BỘ đơn, lấy kho gần nhất bất kỳ
        if (bestWarehouse == null) {
            minDistance = Double.MAX_VALUE;
            for (Warehouse wh : warehouses) {
                double distance = calculateHaversineDistance(
                        userLat, userLng, 
                        wh.getLatitude() != null ? wh.getLatitude() : 21.0285, 
                        wh.getLongitude() != null ? wh.getLongitude() : 105.8542
                );
                if (distance < minDistance) {
                    minDistance = distance;
                    bestWarehouse = wh;
                }
            }
        }

        return bestWarehouse;
    }

    /**
     * Trừ tồn kho tại kho được chỉ định
     */
    public void deductStock(Warehouse warehouse, ProductVariant variant, int quantity) {
        if (warehouse == null || variant == null) return;

        Optional<WarehouseInventory> invOpt = warehouseInventoryRepository
                .findByWarehouseIdAndProductVariantId(warehouse.getId(), variant.getId());

        WarehouseInventory inventory;
        if (invOpt.isPresent()) {
            inventory = invOpt.get();
            inventory.setQuantity(Math.max(0, inventory.getQuantity() - quantity));
        } else {
            // Nếu chưa có mapping trong kho, tạo mới với stock ban đầu âm/0 (thường do lỗi đồng bộ)
            inventory = WarehouseInventory.builder()
                    .warehouse(warehouse)
                    .productVariant(variant)
                    .quantity(0)
                    .inventoryThreshold(5)
                    .build();
        }
        warehouseInventoryRepository.save(inventory);

        // Kiểm tra và gửi cảnh báo hết hàng (Stock Alert)
        if (inventory.getQuantity() <= inventory.getInventoryThreshold()) {
            triggerStockAlert(inventory);
        }
    }

    /**
     * Hoàn trả tồn kho khi đơn hàng bị hủy
     */
    public void restoreStock(Warehouse warehouse, ProductVariant variant, int quantity) {
        if (warehouse == null || variant == null) return;

        Optional<WarehouseInventory> invOpt = warehouseInventoryRepository
                .findByWarehouseIdAndProductVariantId(warehouse.getId(), variant.getId());

        WarehouseInventory inventory;
        if (invOpt.isPresent()) {
            inventory = invOpt.get();
            inventory.setQuantity(inventory.getQuantity() + quantity);
        } else {
            inventory = WarehouseInventory.builder()
                    .warehouse(warehouse)
                    .productVariant(variant)
                    .quantity(quantity)
                    .inventoryThreshold(5)
                    .build();
        }
        warehouseInventoryRepository.save(inventory);
    }

    /**
     * Kích hoạt gửi email cảnh báo và tạo thông báo hệ thống khi hết hàng
     */
    private void triggerStockAlert(WarehouseInventory inventory) {
        ProductVariant variant = inventory.getProductVariant();
        Product product = variant.getProduct();
        Warehouse warehouse = inventory.getWarehouse();
        User seller = product.getShop() != null ? product.getShop().getUser() : null;

        String alertTitle = "⚠️ Cảnh báo: Tồn kho chạm ngưỡng tối thiểu!";
        String alertMsg = String.format("Sản phẩm biến thể '%s' (SKU: %s) tại kho '%s' chỉ còn %d sản phẩm (Dưới ngưỡng %d). Vui lòng nhập hàng thêm!",
                variant.getName(), variant.getSku(), warehouse.getName(), inventory.getQuantity(), inventory.getInventoryThreshold());

        // 1. Tạo thông báo hệ thống cho Seller
        if (seller != null) {
            notificationService.createNotification(
                    seller.getId(),
                    alertTitle,
                    alertMsg,
                    null,
                    "/seller/inventory",
                    NotificationType.SYSTEM
            );

            // 2. Gửi email cảnh báo cho Seller
            if (seller.getEmail() != null) {
                emailService.sendLowStockAlertEmail(
                        seller.getEmail(),
                        variant.getName(),
                        variant.getSku(),
                        warehouse.getName(),
                        inventory.getQuantity()
                );
            }
        }
    }

    /**
     * Tính khoảng cách giữa 2 điểm địa lý bằng Haversine formula (trả về km)
     */
    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Bán kính Trái Đất (km)
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
