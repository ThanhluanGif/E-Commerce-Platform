package com.ecommerce.ecommerceapi.scheduler;

import com.ecommerce.ecommerceapi.entity.FlashSale;
import com.ecommerce.ecommerceapi.entity.FlashSaleReminder;
import com.ecommerce.ecommerceapi.entity.NotificationType;
import com.ecommerce.ecommerceapi.repository.FlashSaleReminderRepository;
import com.ecommerce.ecommerceapi.repository.FlashSaleRepository;
import com.ecommerce.ecommerceapi.service.EmailService;
import com.ecommerce.ecommerceapi.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class FlashSaleReminderScheduler {

    @Autowired
    private FlashSaleRepository flashSaleRepository;

    @Autowired
    private FlashSaleReminderRepository flashSaleReminderRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private EmailService emailService;

    @Scheduled(cron = "0 * * * * *") // Chạy mỗi phút
    @Transactional
    public void sendFlashSaleReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime fiveMinutesLater = now.plusMinutes(5);

        // Tìm các chương trình Flash Sale bắt đầu trong 5 phút tới
        List<FlashSale> upcomingSales = flashSaleRepository.findByActiveTrueAndStartTimeBetween(now, fiveMinutesLater);

        for (FlashSale fs : upcomingSales) {
            // Tìm các đăng ký nhắc nhở chưa gửi
            List<FlashSaleReminder> reminders = flashSaleReminderRepository.findByFlashSaleIdAndNotifiedFalse(fs.getId());

            for (FlashSaleReminder reminder : reminders) {
                String title = "⚡ Flash Sale sắp diễn ra!";
                String body = "Chương trình Flash Sale '" + fs.getName() + "' sẽ bắt đầu sau ít phút nữa! Hãy chuẩn bị săn deal ngay!";
                String actionUrl = "/flash-sale";

                // 1. Tạo thông báo trong hệ thống và gửi qua WebSocket
                notificationService.createNotification(
                        reminder.getUser().getId(),
                        title,
                        body,
                        null,
                        actionUrl,
                        NotificationType.PROMOTION
                );

                // 2. Gửi email nhắc nhở
                try {
                    emailService.sendFlashSaleReminderEmail(reminder.getUser(), fs);
                } catch (Exception e) {
                    System.err.println("Gửi email nhắc nhở Flash Sale thất bại: " + e.getMessage());
                }

                // 3. Đánh dấu đã gửi thông báo
                reminder.setNotified(true);
                flashSaleReminderRepository.save(reminder);
            }
        }
    }
}
