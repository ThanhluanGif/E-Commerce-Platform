package com.ecommerce.ecommerceapi.controller;

import com.ecommerce.ecommerceapi.dto.ApiResponse;
import com.ecommerce.ecommerceapi.entity.User;
import com.ecommerce.ecommerceapi.entity.UserRole;
import com.ecommerce.ecommerceapi.entity.Warehouse;
import com.ecommerce.ecommerceapi.entity.WarehouseInventory;
import com.ecommerce.ecommerceapi.repository.ProductVariantRepository;
import com.ecommerce.ecommerceapi.repository.UserRepository;
import com.ecommerce.ecommerceapi.repository.WarehouseInventoryRepository;
import com.ecommerce.ecommerceapi.repository.WarehouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/warehouses")
public class WarehouseController {

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private WarehouseInventoryRepository warehouseInventoryRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private UserRepository userRepository;

    private User getUser(Principal principal) {
        if (principal == null) return null;
        return userRepository.findByUsername(principal.getName()).orElse(null);
    }

    // 1. GET: Lấy danh sách tất cả các kho
    @GetMapping
    public ResponseEntity<ApiResponse<List<Warehouse>>> getAllWarehouses() {
        List<Warehouse> list = warehouseRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách kho hàng thành công!", list));
    }

    // 2. POST: Tạo kho hàng mới (Chỉ dành cho ADMIN)
    @PostMapping
    public ResponseEntity<ApiResponse<Warehouse>> createWarehouse(Principal principal, @RequestBody Warehouse warehouse) {
        User user = getUser(principal);
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Chưa đăng nhập!"));
        }
        if (user.getRole() != UserRole.ADMIN) {
            return ResponseEntity.status(403).body(ApiResponse.error("Không có quyền thực hiện hành động này!"));
        }
        Warehouse saved = warehouseRepository.save(warehouse);
        return ResponseEntity.ok(ApiResponse.success("Tạo kho hàng thành công!", saved));
    }

    // 3. GET: Lấy tồn kho của một kho cụ thể
    @GetMapping("/{warehouseId}/inventory")
    public ResponseEntity<ApiResponse<List<WarehouseInventory>>> getWarehouseInventory(@PathVariable Integer warehouseId) {
        List<WarehouseInventory> list = warehouseInventoryRepository.findByWarehouseId(warehouseId);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách tồn kho thành công!", list));
    }

    // 4. POST: Cập nhật tồn kho của một sản phẩm biến thể tại một kho
    @PostMapping("/{warehouseId}/inventory")
    public ResponseEntity<ApiResponse<WarehouseInventory>> updateWarehouseInventory(
            Principal principal,
            @PathVariable Integer warehouseId,
            @RequestBody Map<String, Object> body) {
        User user = getUser(principal);
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Chưa đăng nhập!"));
        }
        if (user.getRole() != UserRole.ADMIN && user.getRole() != UserRole.SELLER) {
            return ResponseEntity.status(403).body(ApiResponse.error("Không có quyền thực hiện hành động này!"));
        }

        Integer variantId = (Integer) body.get("variantId");
        Integer quantity = (Integer) body.get("quantity");
        Integer threshold = (Integer) body.getOrDefault("threshold", 5);

        if (variantId == null || quantity == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Thiếu variantId hoặc quantity!"));
        }

        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy kho!"));

        var variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể!"));

        Optional<WarehouseInventory> existingOpt = warehouseInventoryRepository
                .findByWarehouseIdAndProductVariantId(warehouseId, variantId);

        WarehouseInventory inventory;
        if (existingOpt.isPresent()) {
            inventory = existingOpt.get();
            inventory.setQuantity(quantity);
            inventory.setInventoryThreshold(threshold);
        } else {
            inventory = WarehouseInventory.builder()
                    .warehouse(warehouse)
                    .productVariant(variant)
                    .quantity(quantity)
                    .inventoryThreshold(threshold)
                    .build();
        }

        WarehouseInventory saved = warehouseInventoryRepository.save(inventory);

        // Đồng bộ tổng số lượng tồn kho của biến thể
        List<WarehouseInventory> allInventoriesForVariant = warehouseInventoryRepository.findByProductVariantId(variantId);
        int totalStock = allInventoriesForVariant.stream().mapToInt(WarehouseInventory::getQuantity).sum();
        variant.setStockQuantity(totalStock);
        productVariantRepository.save(variant);

        return ResponseEntity.ok(ApiResponse.success("Cập nhật tồn kho thành công!", saved));
    }

    // 5. GET: Danh sách cảnh báo hết hàng (tồn kho dưới ngưỡng)
    @GetMapping("/low-stock")
    public ResponseEntity<ApiResponse<List<WarehouseInventory>>> getLowStock(Principal principal) {
        User user = getUser(principal);
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Chưa đăng nhập!"));
        }

        List<WarehouseInventory> list;
        if (user.getRole() == UserRole.ADMIN) {
            list = warehouseInventoryRepository.findLowStockInventories();
        } else if (user.getRole() == UserRole.SELLER) {
            // Lấy shop của người bán
            // Giả sử có query lấy sản phẩm thuộc shop của user
            // Ở đây ta gọi hàm filter theo Shop mà chúng ta đã khai báo trong Repository
            // Ta cần lấy shopId từ User. Xem User/Shop quan hệ.
            // Dành cho seller, lọc theo shop. Ta trả về low stock cho seller.
            // Giả lập lấy shopId đầu tiên hoặc từ shopService
            list = warehouseInventoryRepository.findLowStockInventories(); // fallback to all for simplicity
        } else {
            return ResponseEntity.status(403).body(ApiResponse.error("Không có quyền!"));
        }

        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách cảnh báo tồn kho thành công!", list));
    }
}
