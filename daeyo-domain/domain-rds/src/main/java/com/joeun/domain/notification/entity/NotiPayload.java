package com.joeun.domain.notification.entity;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotiPayload {
    private String title;
    private String message;

    @Builder
    public NotiPayload(String title, String message) {
        this.title = title;
        this.message = message;
    }
}
