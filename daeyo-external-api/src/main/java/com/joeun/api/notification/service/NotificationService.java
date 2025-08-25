package com.joeun.api.notification.service;

import com.joeun.api.notification.dto.NotificationReadMarkRequest;
import com.joeun.api.notification.dto.NotificationResponse;
import com.joeun.domain.notification.entity.NotiPayload;
import com.joeun.domain.notification.entity.NotiType;
import com.joeun.domain.notification.entity.NotiUser;
import com.joeun.domain.notification.entity.Notification;
import com.joeun.global.common.dto.NotificationRequest;
import com.joeun.port.notification.NotificationInfraService;
import com.joeun.service.notification.NotiUserDomainService;
import com.joeun.service.notification.NotificationDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationDomainService notificationDomainService;
    private final NotificationInfraService notificationInfraService;
    private final NotiUserDomainService notiUserDomainService;

    @EventListener
    public void sendNotification(NotificationRequest request) {
        NotiType notiType = request.notiType();
        Long userId = request.userId();

        Notification notification = Notification.builder()
                .payload(
                        NotiPayload.builder()
                                .title(notiType.getTitle())
                                .message(notiType.getMessage())
                                .build()
                )
                .userId(userId)
                .notiType(notiType)
                .isRead(false)
                .build();

        NotiUser notiUser = notiUserDomainService.findNotiUserByUserId(userId);
        try {
            notificationInfraService.sendNotification(notification, notiUser);
            notificationDomainService.saveNotification(notification);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send notification", e);
        }
    }

    public List<NotificationResponse> getNotificationsByUserId(Long userId) {
        List<Notification> notifications = notificationDomainService.findNotificationsByUserIdAndIsReadFalse(userId);
        return notifications.stream()
                .map(NotificationResponse::from)
                .toList();
    }

    public void markNotificationsAsRead(Long userId, NotificationReadMarkRequest request) {
        notificationDomainService.markAsRead(request.notificationId(), userId);
    }
}
