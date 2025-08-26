package com.joeun.domain.notification.entity;

import com.joeun.domain.users.entity.User;
import com.joeun.global.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class NotiUser extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "noti_user_id")
    private Long notiUserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Embedded
    @Column(name = "device_info")
    private DeviceInfo deviceInfo;

    @Column(name = "is_active")
    private boolean isActive;

    @Builder
    public NotiUser(User user, DeviceInfo deviceInfo, boolean isActive) {
        this.user = user;
        this.deviceInfo = deviceInfo;
        this.isActive = isActive;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }
}
