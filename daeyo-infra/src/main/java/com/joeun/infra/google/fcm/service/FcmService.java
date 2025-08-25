package com.joeun.infra.google.fcm.service;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.joeun.domain.notification.entity.NotiUser;
import com.joeun.domain.notification.entity.Notification;
import com.joeun.port.notification.NotificationInfraService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FcmService implements NotificationInfraService {

    @Override
    public void sendNotification(Notification notification, NotiUser notiUser) {
        Message message = Message.builder()
                .setToken(notiUser.getDeviceInfo().getToken())
                .putData("title", notification.getPayload().getTitle())
                .putData("body", notification.getPayload().getMessage())
                .build();

        try {
            String response = FirebaseMessaging.getInstance()
                    .send(message);
            log.info("Successfully sent message: {}", response);
        } catch (Exception e) {
            log.error("Error sending FCM message: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send notification", e);
        }

    }
}
