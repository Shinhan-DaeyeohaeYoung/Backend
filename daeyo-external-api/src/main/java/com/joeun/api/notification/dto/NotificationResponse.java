package com.joeun.api.notification.dto;

import com.joeun.domain.notification.entity.Notification;
import lombok.Builder;

public record NotificationResponse(
        Long notificationId,
        String title,
        String content,
        Long userId
        ) {

    @Builder
    public NotificationResponse {}

    public static NotificationResponse from(Notification notification) {
        return NotificationResponse.builder()
                .notificationId(notification.getNotificationId())
                .title(notification.getPayload().getTitle())
                .content(notification.getPayload().getMessage())
                .userId(notification.getUser().getId())
                .build();
    }
}
