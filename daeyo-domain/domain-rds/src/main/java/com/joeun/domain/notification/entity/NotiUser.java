package com.joeun.domain.notification.entity;

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

    // Todo: 추 후 User 엔티티와 연관관계 설정
    @Column(name = "user_id")
    private Long userId;

    @Embedded
    @Column(name = "device_info")
    private DeviceInfo deviceInfo;

    @Column(name = "is_active")
    private boolean isActive;

    @Builder
    public NotiUser(Long userId, DeviceInfo deviceInfo, boolean isActive) {
        this.userId = userId;
        this.deviceInfo = deviceInfo;
        this.isActive = isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
