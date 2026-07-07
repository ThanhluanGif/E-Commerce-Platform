package com.ecommerce.ecommerceapi.service;

import com.ecommerce.ecommerceapi.entity.Order;
import com.ecommerce.ecommerceapi.entity.Referral;
import com.ecommerce.ecommerceapi.entity.User;
import com.ecommerce.ecommerceapi.entity.NotificationType;
import com.ecommerce.ecommerceapi.repository.ReferralRepository;
import com.ecommerce.ecommerceapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ReferralService {

    @Autowired
    private ReferralRepository referralRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LoyaltyService loyaltyService;

    @Autowired
    private NotificationService notificationService;

    public void processOrderDelivery(Order order) {
        Integer refereeId = order.getUser().getId();
        
        // Tìm xem user đặt hàng có phải là người được giới thiệu không
        Optional<Referral> referralOpt = referralRepository.findByRefereeId(refereeId);
        
        if (referralOpt.isPresent()) {
            Referral referral = referralOpt.get();
            
            // Chỉ xử lý nếu trạng thái giới thiệu là PENDING và chưa thưởng
            if ("PENDING".equals(referral.getStatus()) && !referral.isRewarded()) {
                User referrer = referral.getReferrer();
                User referee = referral.getReferee();
                
                // Cập nhật trạng thái Referral
                referral.setStatus("COMPLETED");
                referral.setCompletedAt(LocalDateTime.now());
                referral.setRewarded(true);
                referralRepository.save(referral);
                
                // Thưởng điểm Loyalty cho người giới thiệu (100 điểm)
                loyaltyService.addPoints(
                        referrer.getId(), 
                        100, 
                        "Điểm thưởng giới thiệu người dùng mới: " + referee.getUsername()
                );
                
                // Tạo thông báo cho người giới thiệu
                String title = "🎉 Nhận điểm thưởng giới thiệu!";
                String message = "Chúc mừng! Bạn đã nhận được 100 điểm thưởng vì người bạn giới thiệu (" 
                        + referee.getUsername() + ") hoàn thành đơn hàng đầu tiên.";
                notificationService.createNotification(
                        referrer.getId(),
                        title,
                        message,
                        null,
                        "/loyalty",
                        NotificationType.PROMOTION
                );
            }
        }
    }

    public List<com.ecommerce.ecommerceapi.dto.ReferralDTO> getReferralsForUser(Integer userId) {
        List<Referral> referrals = referralRepository.findByReferrerId(userId);
        return referrals.stream().map(ref -> com.ecommerce.ecommerceapi.dto.ReferralDTO.builder()
                .id(ref.getId())
                .refereeUsername(ref.getReferee().getUsername())
                .refereeEmail(ref.getReferee().getEmail())
                .status(ref.getStatus())
                .rewarded(ref.isRewarded())
                .createdAt(ref.getCreatedAt())
                .completedAt(ref.getCompletedAt())
                .build()).collect(java.util.stream.Collectors.toList());
    }
}
