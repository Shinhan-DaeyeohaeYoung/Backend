package com.joeun.global.dto;

import com.joeun.domain.notification.entity.NotiType;
import lombok.Builder;

public record NotificationRequest(
        NotiType notiType,
        Long userId
) {
    @Builder
    public NotificationRequest {
    }
}
