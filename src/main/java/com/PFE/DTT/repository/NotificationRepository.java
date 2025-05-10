package com.PFE.DTT.repository;

import com.PFE.DTT.model.Notification;
import com.PFE.DTT.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderByTimeDesc(Long userId);

    List<Notification> findByUserId(Long userId);

}