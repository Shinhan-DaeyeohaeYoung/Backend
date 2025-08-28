package com.joeun.domain.notification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeviceInfo {
    @Column(name = "token")
    private String token;

    @Column(name = "device_type")
    @Enumerated(EnumType.STRING)
    private DeviceType deviceType;

    @Builder
    public DeviceInfo(String token, DeviceType deviceType) {
        this.token = token;
        this.deviceType = deviceType;
    }
}
