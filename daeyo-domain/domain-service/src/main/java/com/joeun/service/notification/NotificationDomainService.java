package com.joeun.service.notification;

import com.joeun.domain.notification.entity.NotiUser;
import com.joeun.domain.notification.entity.Notification;
import com.joeun.domain.notification.service.NotificationRdsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationDomainService {

    private final NotificationRdsService notificationRdsService;

    public void saveNotification(Notification notification) {
        notificationRdsService.saveNotification(notification);
    }

    public Notification findNotificationById(Long id) {
        return notificationRdsService.findNotificationById(id);
    }

    public void deleteNotification(Long id) {
        notificationRdsService.deleteNotification(id);
    }

    public List<Notification> findNotificationsByUserId(Long userId) {
        return notificationRdsService.findNotificationsByUserId(userId);
    }

    public List<Notification> findNotificationsByUserIdAndIsReadFalse(Long userId) {
        return notificationRdsService.findNotificationsByUserIdAndIsReadFalse(userId);
    }

    public void markAsRead(Long notificationId, Long userId) {
        notificationRdsService.markAsRead(notificationId, userId);
    }
}
