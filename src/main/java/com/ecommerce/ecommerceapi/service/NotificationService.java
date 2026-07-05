package com.ecommerce.ecommerceapi.service;

import com.ecommerce.ecommerceapi.entity.Notification;
import com.ecommerce.ecommerceapi.entity.User;
import com.ecommerce.ecommerceapi.entity.NotificationType;
import com.ecommerce.ecommerceapi.repository.NotificationRepository;
import com.ecommerce.ecommerceapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public Notification createNotification(Integer userId, String title, String body, String imageUrl, String actionUrl) {
        return createNotification(userId, title, body, imageUrl, actionUrl, NotificationType.SYSTEM);
    }

    public Notification createNotification(Integer userId, String title, String body, String imageUrl, String actionUrl, NotificationType type) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return null;

        Notification notif = Notification.builder()
                .user(user)
                .title(title)
                .body(body)
                .imageUrl(imageUrl)
                .actionUrl(actionUrl)
                .type(type)
                .build();

        Notification saved = notificationRepository.save(notif);
        // Gửi thông báo realtime qua WebSocket
        try {
            messagingTemplate.convertAndSend("/topic/notifications/" + userId, saved);
        } catch (Exception e) {
            // Log error but don't break transaction
            System.err.println("Failed to send WebSocket notification: " + e.getMessage());
        }
        return saved;
    }

    public List<Notification> getMyNotifications(Integer userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public long getUnreadCount(Integer userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    public void markAllAsRead(Integer userId) {
        List<Notification> list = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        for (Notification n : list) {
            if (!n.isRead()) {
                n.setRead(true);
                notificationRepository.save(n);
            }
        }
    }

    public void markAsRead(Integer id, Integer userId) {
        Notification n = notificationRepository.findById(id).orElse(null);
        if (n != null && n.getUser().getId().equals(userId)) {
            n.setRead(true);
            notificationRepository.save(n);
        }
    }
}
