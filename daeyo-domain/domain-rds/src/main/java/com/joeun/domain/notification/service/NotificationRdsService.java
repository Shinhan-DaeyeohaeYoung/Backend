package com.joeun.domain.notification.service;

import com.joeun.domain.notification.entity.NotiUser;
import com.joeun.domain.notification.entity.Notification;
import com.joeun.domain.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationRdsService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void saveNotification(Notification notification) {
        notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public Notification findNotificationById(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found with id: " + id));
    }

    @Transactional
    public void deleteNotification(Long id) {
        Notification notification = findNotificationById(id);
        notificationRepository.delete(notification);
    }

    @Transactional(readOnly = true)
    public List<Notification> findNotificationsByUserId(Long userId) {
        return notificationRepository.findByUserId(userId);
    }

    public List<Notification> findNotificationsByUserIdAndIsReadFalse(Long userId) {
        return notificationRepository.findNotificationsByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notifications = findNotificationById(notificationId);
        if (notifications.getUserId().equals(userId)) {
            notifications.markRead();
        } else {
            throw new IllegalArgumentException("User does not have permission to mark this notification as read.");
        }
    }
}
