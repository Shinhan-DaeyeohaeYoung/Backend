package com.joeun.domain.notification.entity;

import com.joeun.global.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Notification extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    // Todo: User 엔티티와 연관관계 설정
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Embedded
    private NotiPayload payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "noti_type")
    private NotiType notiType;

    @Column(name = "is_read", nullable = false)
    private boolean isRead;

    @Builder
    public Notification(Long userId, NotiPayload payload, NotiType notiType, boolean isRead) {
        this.userId = userId;
        this.payload = payload;
        this.notiType = notiType;
        this.isRead = isRead;
    }

    public void markRead() {
        this.isRead = true;
    }
}
