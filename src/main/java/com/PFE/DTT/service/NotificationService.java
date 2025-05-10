package com.PFE.DTT.service;

import com.PFE.DTT.dto.NotificationDTO;
import com.PFE.DTT.model.Notification;
import com.PFE.DTT.model.User;
import com.PFE.DTT.repository.NotificationRepository;
import com.PFE.DTT.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public Notification createNotification(NotificationDTO dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Notification notif = new Notification();
        notif.setDescription(dto.getDescription());
        notif.setLink(dto.getLink());
        notif.setUser(user);
        notif.setTime(LocalDateTime.now());
        notif.setNotificationType(dto.getNotificationType());
        notif.setSeen(false);

        Notification saved = notificationRepository.save(notif);

        messagingTemplate.convertAndSendToUser(
                String.valueOf(user.getId()),
                "/queue/notifications",
                saved
        );

        return saved;
    }

    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByTimeDesc(userId);
    }

    // ✅ New method to mark notification as seen
    public Notification markAsSeen(Long notificationId, User currentUser) {
        Notification notif = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notif.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Not authorized to update this notification");
        }

        notif.setSeen(true);
        Notification updated = notificationRepository.save(notif);

        // 🔁 Push updated notification via WebSocket
        messagingTemplate.convertAndSendToUser(
                String.valueOf(currentUser.getId()),
                "/queue/notifications",
                updated
        );

        return updated;
    }

    public void deleteNotification(Long id, User currentUser) {
        Notification notif = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notif.getUser().getId().equals(currentUser.getId())) {
            throw new SecurityException("You are not authorized to delete this notification.");
        }

        notificationRepository.delete(notif);

        // 🔄 WebSocket push for deletion
        messagingTemplate.convertAndSendToUser(
                String.valueOf(currentUser.getId()),
                "/queue/notifications/deleted",
                id
        );
    }

}
