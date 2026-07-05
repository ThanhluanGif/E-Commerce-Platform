package com.ecommerce.ecommerceapi.service;

import com.ecommerce.ecommerceapi.entity.*;
import com.ecommerce.ecommerceapi.repository.PointTransactionRepository;
import com.ecommerce.ecommerceapi.repository.UserPointsRepository;
import com.ecommerce.ecommerceapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class LoyaltyService {

    @Autowired
    private UserPointsRepository userPointsRepository;

    @Autowired
    private PointTransactionRepository pointTransactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    public UserPoints getUserPoints(Integer userId) {
        return userPointsRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId).orElseThrow();
                    UserPoints up = UserPoints.builder()
                            .user(user)
                            .points(0)
                            .tier(MembershipTier.BRONZE)
                            .build();
                    return userPointsRepository.save(up);
                });
    }

    public List<PointTransaction> getPointHistory(Integer userId) {
        return pointTransactionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public void addPoints(Integer userId, Integer pointsToAdd, String reason) {
        if (pointsToAdd <= 0) return;

        UserPoints userPoints = getUserPoints(userId);
        userPoints.setPoints(userPoints.getPoints() + pointsToAdd);
        
        // Cập nhật hạng thành viên
        MembershipTier oldTier = userPoints.getTier();
        MembershipTier newTier = calculateTier(userPoints.getPoints());
        userPoints.setTier(newTier);
        userPointsRepository.save(userPoints);

        // Lưu giao dịch điểm
        PointTransaction txn = PointTransaction.builder()
                .user(userPoints.getUser())
                .points(pointsToAdd)
                .reason(reason)
                .build();
        pointTransactionRepository.save(txn);

        // Gửi thông báo
        notificationService.createNotification(
                userId,
                "Tích lũy điểm thưởng",
                "Bạn được cộng +" + pointsToAdd + " điểm thưởng cho hoạt động: " + reason + ".",
                null,
                "/loyalty",
                NotificationType.SYSTEM
        );

        if (newTier != oldTier) {
            notificationService.createNotification(
                    userId,
                    "Nâng hạng thành viên",
                    "Chúc mừng! Bạn đã được thăng hạng thành viên lên: " + newTier.name() + ".",
                    null,
                    "/loyalty",
                    NotificationType.SYSTEM
            );
        }
    }

    public void deductPoints(Integer userId, Integer pointsToDeduct, String reason) {
        if (pointsToDeduct <= 0) return;

        UserPoints userPoints = getUserPoints(userId);
        int currentPoints = userPoints.getPoints();
        int finalPoints = Math.max(0, currentPoints - pointsToDeduct);
        userPoints.setPoints(finalPoints);

        MembershipTier oldTier = userPoints.getTier();
        MembershipTier newTier = calculateTier(finalPoints);
        userPoints.setTier(newTier);
        userPointsRepository.save(userPoints);

        PointTransaction txn = PointTransaction.builder()
                .user(userPoints.getUser())
                .points(-pointsToDeduct)
                .reason(reason)
                .build();
        pointTransactionRepository.save(txn);

        notificationService.createNotification(
                userId,
                "Khấu trừ điểm thưởng",
                "Tài khoản của bạn bị trừ -" + pointsToDeduct + " điểm cho lý do: " + reason + ".",
                null,
                "/loyalty",
                NotificationType.SYSTEM
        );
    }

    private MembershipTier calculateTier(int points) {
        if (points >= 5000) return MembershipTier.DIAMOND;
        if (points >= 2000) return MembershipTier.PLATINUM;
        if (points >= 500) return MembershipTier.GOLD;
        if (points >= 100) return MembershipTier.SILVER;
        return MembershipTier.BRONZE;
    }
}
