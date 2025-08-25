package com.joeun.global.common.dto;

import com.joeun.domain.notification.entity.NotiType;

public record NotificationRequest(
        NotiType notiType,
        Long userId
) {
}
