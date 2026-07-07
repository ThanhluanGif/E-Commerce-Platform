package com.ecommerce.ecommerceapi.scheduler;

import com.ecommerce.ecommerceapi.dto.VoucherDTO;
import com.ecommerce.ecommerceapi.entity.*;
import com.ecommerce.ecommerceapi.repository.CartItemRepository;
import com.ecommerce.ecommerceapi.repository.CartRecoveryLogRepository;
import com.ecommerce.ecommerceapi.repository.UserRepository;
import com.ecommerce.ecommerceapi.service.EmailService;
import com.ecommerce.ecommerceapi.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class CartRecoveryScheduler {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartRecoveryLogRepository cartRecoveryLogRepository;

    @Autowired
    private VoucherService voucherService;

    @Autowired
    private EmailService emailService;

    @Scheduled(cron = "0 0/30 * * * *") // Chạy mỗi 30 phút
    @Transactional
    public void recoverAbandonedCarts() {
        LocalDateTime twoHoursAgo = LocalDateTime.now().minusHours(2);

        // Lấy tất cả user có cart items sửa đổi hơn 2 tiếng trước
        List<Integer> userIds = cartItemRepository.findUserIdsWithCartItemsOlderThan(twoHoursAgo);

        for (Integer userId : userIds) {
            // Kiểm tra xem giỏ hàng thực tế của user có rỗng không
            List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
            if (cartItems.isEmpty()) {
                continue;
            }

            // Kiểm tra log gửi email khôi phục gần nhất
            Optional<CartRecoveryLog> logOpt = cartRecoveryLogRepository.findByUserId(userId);
            if (logOpt.isPresent()) {
                CartRecoveryLog log = logOpt.get();
                // Không gửi thêm nếu đã gửi trong vòng 24 tiếng qua
                if (log.getLastSentAt().isAfter(LocalDateTime.now().minusDays(1))) {
                    continue;
                }
            }

            User user = userRepository.findById(userId).orElse(null);
            if (user == null || user.getEmail() == null) {
                continue;
            }

            // Tạo mã voucher ngẫu nhiên giảm giá 10%
            String voucherCode = "RECOVERY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            
            try {
                VoucherDTO voucherDTO = VoucherDTO.builder()
                        .code(voucherCode)
                        .type(VoucherType.PERCENT)
                        .value(BigDecimal.valueOf(10)) // Giảm 10%
                        .minOrderValue(BigDecimal.ZERO)
                        .maxDiscountValue(BigDecimal.valueOf(50000)) // Tối đa 50,000 VND
                        .usageLimit(1)
                        .scope("PLATFORM")
                        .startDate(LocalDateTime.now())
                        .endDate(LocalDateTime.now().plusDays(3)) // Hạn dùng 3 ngày
                        .active(true)
                        .build();

                voucherService.createVoucher(voucherDTO);
                
                // Tự động thu thập/claim voucher này cho user luôn
                voucherService.claimVoucher(userId, voucherCode);

                // Gửi email khôi phục giỏ hàng kèm mã voucher
                emailService.sendAbandonedCartEmail(user, voucherCode, "10% (Tối đa 50,000đ)");

                // Ghi nhận log
                CartRecoveryLog log = logOpt.orElse(new CartRecoveryLog());
                log.setUser(user);
                log.setLastSentAt(LocalDateTime.now());
                log.setVoucherCode(voucherCode);
                cartRecoveryLogRepository.save(log);

                System.out.println("Đã gửi email khôi phục giỏ hàng cho user: " + user.getUsername() + ", Mã voucher: " + voucherCode);
            } catch (Exception e) {
                System.err.println("Gửi email khôi phục giỏ hàng lỗi cho user " + user.getUsername() + ": " + e.getMessage());
            }
        }
    }
}
