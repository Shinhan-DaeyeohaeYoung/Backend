package com.joeun.api.notification.dto;

import com.joeun.domain.notification.entity.DeviceType;

public record NotiUserRequest(
        Long userId,
        String token,
        DeviceType deviceType,
        boolean isActive
) {
}
