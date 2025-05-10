package com.PFE.DTT.controller;

import com.PFE.DTT.dto.NotificationDTO;
import com.PFE.DTT.model.Notification;
import com.PFE.DTT.model.User;
import com.PFE.DTT.repository.NotificationRepository;
import com.PFE.DTT.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @PostMapping("/add")
    public ResponseEntity<?> createNotification(@RequestBody NotificationDTO dto) {
        notificationService.createNotification(dto);
        return ResponseEntity.ok("Notification sent");
    }

    @GetMapping("/user")
    public ResponseEntity<List<Notification>> getUserNotifications(@AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<Notification> notifications = notificationRepository.findByUserId(currentUser.getId());
        if (notifications == null) {
            notifications = new ArrayList<>(); // never return null
        }

        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/{id}/seen")
    public ResponseEntity<?> markAsSeen(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        Notification updated = notificationService.markAsSeen(id, currentUser);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteNotification(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        notificationService.deleteNotification(id, currentUser);
        return ResponseEntity.ok("Notification deleted");
    }



}
