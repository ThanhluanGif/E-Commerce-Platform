package com.ecommerce.ecommerceapi.service;

import com.ecommerce.ecommerceapi.dto.VoucherDTO;
import com.ecommerce.ecommerceapi.entity.Shop;
import com.ecommerce.ecommerceapi.entity.User;
import com.ecommerce.ecommerceapi.entity.UserVoucher;
import com.ecommerce.ecommerceapi.entity.Voucher;
import com.ecommerce.ecommerceapi.exception.BadRequestException;
import com.ecommerce.ecommerceapi.exception.ResourceNotFoundException;
import com.ecommerce.ecommerceapi.repository.ShopRepository;
import com.ecommerce.ecommerceapi.repository.UserRepository;
import com.ecommerce.ecommerceapi.repository.UserVoucherRepository;
import com.ecommerce.ecommerceapi.repository.VoucherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class VoucherService {

    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private UserVoucherRepository userVoucherRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShopRepository shopRepository;

    public Voucher createVoucher(VoucherDTO dto) {
        if (voucherRepository.findByCode(dto.getCode()).isPresent()) {
            throw new BadRequestException("Mã giảm giá đã tồn tại!");
        }

        Shop shop = null;
        if ("SHOP".equalsIgnoreCase(dto.getScope()) && dto.getShopId() != null) {
            shop = shopRepository.findById(dto.getShopId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy shop!"));
        }

        Voucher voucher = Voucher.builder()
                .code(dto.getCode().toUpperCase())
                .type(dto.getType())
                .value(dto.getValue())
                .minOrderValue(dto.getMinOrderValue() != null ? dto.getMinOrderValue() : BigDecimal.ZERO)
                .maxDiscountValue(dto.getMaxDiscountValue())
                .usageLimit(dto.getUsageLimit())
                .scope(dto.getScope() != null ? dto.getScope().toUpperCase() : "PLATFORM")
                .shop(shop)
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .active(dto.getActive() != null ? dto.getActive() : true)
                .build();

        return voucherRepository.save(voucher);
    }

    public List<VoucherDTO> getActiveVouchers() {
        LocalDateTime now = LocalDateTime.now();
        return voucherRepository.findByActiveTrue().stream()
                .filter(v -> (v.getStartDate() == null || v.getStartDate().isBefore(now)) &&
                             (v.getEndDate() == null || v.getEndDate().isAfter(now)))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<VoucherDTO> getActiveVouchersByShop(Integer shopId) {
        LocalDateTime now = LocalDateTime.now();
        return voucherRepository.findByShopIdAndActiveTrue(shopId).stream()
                .filter(v -> (v.getStartDate() == null || v.getStartDate().isBefore(now)) &&
                             (v.getEndDate() == null || v.getEndDate().isAfter(now)))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public UserVoucher claimVoucher(Integer userId, String code) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng!"));
        Voucher voucher = voucherRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Mã giảm giá không tồn tại hoặc đã hết hạn!"));

        LocalDateTime now = LocalDateTime.now();
        if (!voucher.isActive() || 
            (voucher.getStartDate() != null && voucher.getStartDate().isAfter(now)) || 
            (voucher.getEndDate() != null && voucher.getEndDate().isBefore(now))) {
            throw new BadRequestException("Mã giảm giá không trong thời gian hoạt động!");
        }

        if (voucher.getUsageLimit() != null && voucher.getUsedCount() >= voucher.getUsageLimit()) {
            throw new BadRequestException("Mã giảm giá đã hết lượt sử dụng!");
        }

        if (userVoucherRepository.findByUserIdAndVoucherId(userId, voucher.getId()).isPresent()) {
            throw new BadRequestException("Bạn đã thu thập mã giảm giá này rồi!");
        }

        UserVoucher uv = UserVoucher.builder()
                .user(user)
                .voucher(voucher)
                .build();

        return userVoucherRepository.save(uv);
    }

    public List<VoucherDTO> getMyVouchers(Integer userId) {
        return userVoucherRepository.findByUserId(userId).stream()
                .filter(uv -> uv.getUsedAt() == null)
                .map(uv -> convertToDTO(uv.getVoucher()))
                .collect(Collectors.toList());
    }

    public BigDecimal calculateDiscount(String code, BigDecimal orderTotal, Integer userId) {
        Voucher voucher = voucherRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Mã giảm giá không hợp lệ!"));

        LocalDateTime now = LocalDateTime.now();
        if (!voucher.isActive() ||
            (voucher.getStartDate() != null && voucher.getStartDate().isAfter(now)) ||
            (voucher.getEndDate() != null && voucher.getEndDate().isBefore(now))) {
            throw new BadRequestException("Mã giảm giá không có hiệu lực!");
        }

        if (voucher.getUsageLimit() != null && voucher.getUsedCount() >= voucher.getUsageLimit()) {
            throw new BadRequestException("Mã giảm giá đã hết lượt sử dụng!");
        }

        if (orderTotal.compareTo(voucher.getMinOrderValue()) < 0) {
            throw new BadRequestException("Đơn hàng chưa đạt giá trị tối thiểu " + voucher.getMinOrderValue() + "đ!");
        }

        BigDecimal discount = BigDecimal.ZERO;
        switch (voucher.getType()) {
            case FIXED:
            case SHIPPING:
                discount = voucher.getValue();
                break;
            case PERCENT:
                discount = orderTotal.multiply(voucher.getValue()).divide(BigDecimal.valueOf(100));
                if (voucher.getMaxDiscountValue() != null && discount.compareTo(voucher.getMaxDiscountValue()) > 0) {
                    discount = voucher.getMaxDiscountValue();
                }
                break;
        }

        if (discount.compareTo(orderTotal) > 0) {
            discount = orderTotal;
        }

        return discount;
    }

    public void markVoucherAsUsed(String code, Integer userId, com.ecommerce.ecommerceapi.entity.Order order) {
        Voucher voucher = voucherRepository.findByCode(code.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Mã giảm giá không hợp lệ!"));

        UserVoucher uv = userVoucherRepository.findByUserIdAndVoucherId(userId, voucher.getId())
                .orElse(null);

        if (uv != null) {
            uv.setUsedAt(LocalDateTime.now());
            uv.setOrder(order);
            userVoucherRepository.save(uv);
        }

        voucher.setUsedCount(voucher.getUsedCount() + 1);
        voucherRepository.save(voucher);
    }

    public VoucherDTO convertToDTO(Voucher voucher) {
        return VoucherDTO.builder()
                .id(voucher.getId())
                .code(voucher.getCode())
                .type(voucher.getType())
                .value(voucher.getValue())
                .minOrderValue(voucher.getMinOrderValue())
                .maxDiscountValue(voucher.getMaxDiscountValue())
                .usageLimit(voucher.getUsageLimit())
                .usedCount(voucher.getUsedCount())
                .scope(voucher.getScope())
                .shopId(voucher.getShop() != null ? voucher.getShop().getId() : null)
                .shopName(voucher.getShop() != null ? voucher.getShop().getName() : null)
                .startDate(voucher.getStartDate())
                .endDate(voucher.getEndDate())
                .active(voucher.isActive())
                .build();
    }
}
