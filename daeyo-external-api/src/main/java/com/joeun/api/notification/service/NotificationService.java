package com.joeun.api.notification.service;

import com.joeun.api.notification.dto.NotificationReadMarkRequest;
import com.joeun.api.notification.dto.NotificationResponse;
import com.joeun.domain.notification.entity.NotiPayload;
import com.joeun.domain.notification.entity.NotiType;
import com.joeun.domain.notification.entity.NotiUser;
import com.joeun.domain.notification.entity.Notification;
import com.joeun.domain.users.entity.User;
import com.joeun.global.dto.NotificationRequest;
import com.joeun.port.notification.NotificationInfraService;
import com.joeun.service.notification.NotiUserDomainService;
import com.joeun.service.notification.NotificationDomainService;
import com.joeun.service.user.UserDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationDomainService notificationDomainService;
    private final NotificationInfraService notificationInfraService;
    private final NotiUserDomainService notiUserDomainService;
    private final UserDomainService userDomainService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendNotification(NotificationRequest request) {
        NotiType notiType = request.notiType();
        Long userId = request.userId();

        User user = userDomainService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        Notification notification = Notification.builder()
                .payload(
                        NotiPayload.builder()
                                .title(notiType.getTitle())
                                .message(notiType.getMessage())
                                .build()
                )
                .user(user)
                .notiType(notiType)
                .isRead(false)
                .build();

        List<NotiUser> notiUser = notiUserDomainService.findNotiUserByUserId(userId);
        try {
            for(NotiUser target : notiUser) {
                notificationInfraService.sendNotification(notification, target);
                notificationDomainService.saveNotification(notification);
            }
        } catch (Exception e) {
            log.error("Failed to send notification to userId {}: {}", userId, e);
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
