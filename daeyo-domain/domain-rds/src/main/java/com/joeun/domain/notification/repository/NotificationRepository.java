package com.joeun.domain.notification.repository;

import com.joeun.domain.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserId(Long userId);

    List<Notification> findNotificationsByUserIdAndIsReadFalse(Long userId);
}
