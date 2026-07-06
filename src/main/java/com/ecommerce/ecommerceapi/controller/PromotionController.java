package com.ecommerce.ecommerceapi.controller;

import com.ecommerce.ecommerceapi.dto.ApiResponse;
import com.ecommerce.ecommerceapi.dto.FlashSaleDTO;
import com.ecommerce.ecommerceapi.dto.FlashSaleItemDTO;
import com.ecommerce.ecommerceapi.dto.VoucherDTO;
import com.ecommerce.ecommerceapi.entity.*;
import com.ecommerce.ecommerceapi.repository.UserRepository;
import com.ecommerce.ecommerceapi.service.FlashSaleService;
import com.ecommerce.ecommerceapi.service.ShopService;
import com.ecommerce.ecommerceapi.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
public class PromotionController {

    @Autowired
    private VoucherService voucherService;

    @Autowired
    private FlashSaleService flashSaleService;

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

    // 1. GET: Lấy các chương trình Flash Sale đang hoạt động
    @GetMapping("/api/flash-sales/active")
    public ResponseEntity<ApiResponse<List<FlashSaleDTO>>> getActiveFlashSales() {
        List<FlashSaleDTO> activeSales = flashSaleService.getActiveFlashSales();
        return ResponseEntity.ok(ApiResponse.success("Lấy chương trình Flash Sale thành công!", activeSales));
    }

    // 2. GET: Lấy các Voucher đang hoạt động trên sàn (hoặc theo shop)
    @GetMapping("/api/vouchers")
    public ResponseEntity<ApiResponse<List<VoucherDTO>>> getVouchers(@RequestParam(required = false) Integer shopId) {
        List<VoucherDTO> vouchers;
        if (shopId != null) {
            vouchers = voucherService.getActiveVouchersByShop(shopId);
        } else {
            vouchers = voucherService.getActiveVouchers();
        }
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách mã giảm giá thành công!", vouchers));
    }

    // 3. POST: User thu thập/claim voucher vào kho ví
    @PostMapping("/api/vouchers/claim")
    public ResponseEntity<ApiResponse<Void>> claimVoucher(Principal principal, @RequestParam String code) {
        Integer userId = getUserId(principal);
        if (userId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Chưa đăng nhập!"));
        }
        voucherService.claimVoucher(userId, code);
        return ResponseEntity.ok(ApiResponse.success("Thu thập mã giảm giá thành công!"));
    }

    // 4. GET: Lấy ví voucher của tôi
    @GetMapping("/api/vouchers/mine")
    public ResponseEntity<ApiResponse<List<VoucherDTO>>> getMyVouchers(Principal principal) {
        Integer userId = getUserId(principal);
        if (userId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Chưa đăng nhập!"));
        }
        List<VoucherDTO> vouchers = voucherService.getMyVouchers(userId);
        return ResponseEntity.ok(ApiResponse.success("Lấy ví voucher thành công!", vouchers));
    }

    // 5. POST: Áp dụng thử voucher khi tính toán checkout
    @PostMapping("/api/checkout/apply-voucher")
    public ResponseEntity<ApiResponse<BigDecimal>> applyVoucher(
            Principal principal,
            @RequestBody Map<String, Object> request
    ) {
        Integer userId = getUserId(principal);
        if (userId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Chưa đăng nhập!"));
        }
        String code = (String) request.get("code");
        Double orderTotalDouble = Double.valueOf(request.get("orderTotal").toString());
        BigDecimal orderTotal = BigDecimal.valueOf(orderTotalDouble);

        BigDecimal discount = voucherService.calculateDiscount(code, orderTotal, userId);
        return ResponseEntity.ok(ApiResponse.success("Áp dụng mã giảm giá thành công!", discount));
    }

    // 6. POST: Admin tạo Voucher toàn sàn
    @PostMapping("/api/admin/vouchers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Voucher>> adminCreateVoucher(@RequestBody VoucherDTO dto) {
        dto.setScope("PLATFORM");
        Voucher v = voucherService.createVoucher(dto);
        return ResponseEntity.ok(ApiResponse.success("Tạo voucher sàn thành công!", v));
    }

    // 7. POST: Seller tạo Voucher của shop
    @PostMapping("/api/seller/vouchers")
    public ResponseEntity<ApiResponse<Voucher>> sellerCreateVoucher(Principal principal, @RequestBody VoucherDTO dto) {
        Integer userId = getUserId(principal);
        Shop shop = shopService.getShopByUserId(userId);
        dto.setScope("SHOP");
        dto.setShopId(shop.getId());
        Voucher v = voucherService.createVoucher(dto);
        return ResponseEntity.ok(ApiResponse.success("Tạo voucher shop thành công!", v));
    }

    // 8. POST: Admin tạo chương trình Flash Sale mới
    @PostMapping("/api/admin/flash-sales")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FlashSale>> adminCreateFlashSale(@RequestBody FlashSaleDTO dto) {
        FlashSale fs = flashSaleService.createFlashSale(dto);
        return ResponseEntity.ok(ApiResponse.success("Tạo chương trình Flash Sale thành công!", fs));
    }

    // 9. POST: Admin thêm sản phẩm vào Flash Sale
    @PostMapping("/api/admin/flash-sales/{id}/items")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FlashSaleItem>> adminAddFlashSaleItem(
            @PathVariable Integer id,
            @RequestBody FlashSaleItemDTO itemDto
    ) {
        FlashSaleItem item = flashSaleService.addProductToFlashSale(id, itemDto);
        return ResponseEntity.ok(ApiResponse.success("Thêm sản phẩm vào Flash Sale thành công!", item));
    }

    // 10. POST: Đăng ký nhận thông báo cho Flash Sale
    @PostMapping("/api/flash-sales/{id}/subscribe")
    public ResponseEntity<ApiResponse<Void>> subscribeFlashSale(Principal principal, @PathVariable Integer id) {
        Integer userId = getUserId(principal);
        if (userId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Chưa đăng nhập!"));
        }
        flashSaleService.subscribeToFlashSale(userId, id);
        return ResponseEntity.ok(ApiResponse.success("Đăng ký nhận thông báo Flash Sale thành công!"));
    }

    // 11. DELETE: Hủy nhận thông báo cho Flash Sale
    @DeleteMapping("/api/flash-sales/{id}/unsubscribe")
    public ResponseEntity<ApiResponse<Void>> unsubscribeFlashSale(Principal principal, @PathVariable Integer id) {
        Integer userId = getUserId(principal);
        if (userId == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("Chưa đăng nhập!"));
        }
        flashSaleService.unsubscribeFromFlashSale(userId, id);
        return ResponseEntity.ok(ApiResponse.success("Hủy nhận thông báo Flash Sale thành công!"));
    }

    // 12. GET: Kiểm tra trạng thái đăng ký nhận thông báo
    @GetMapping("/api/flash-sales/{id}/is-subscribed")
    public ResponseEntity<ApiResponse<Boolean>> isSubscribedFlashSale(Principal principal, @PathVariable Integer id) {
        Integer userId = getUserId(principal);
        if (userId == null) {
            return ResponseEntity.ok(ApiResponse.success("Chưa đăng nhập", false));
        }
        boolean status = flashSaleService.isSubscribed(userId, id);
        return ResponseEntity.ok(ApiResponse.success("Lấy trạng thái đăng ký thành công!", status));
    }
}
