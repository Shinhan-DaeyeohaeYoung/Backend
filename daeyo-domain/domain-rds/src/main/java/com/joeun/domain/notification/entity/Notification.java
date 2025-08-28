package com.joeun.domain.notification.entity;

import com.joeun.domain.users.entity.User;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Embedded
    private NotiPayload payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "noti_type")
    private NotiType notiType;

    @Column(name = "is_read", nullable = false)
    private boolean isRead;

    @Builder
    public Notification(User user, NotiPayload payload, NotiType notiType, boolean isRead) {
        this.user = user;
        this.payload = payload;
        this.notiType = notiType;
        this.isRead = isRead;
    }

    public void markRead() {
        this.isRead = true;
    }
}
