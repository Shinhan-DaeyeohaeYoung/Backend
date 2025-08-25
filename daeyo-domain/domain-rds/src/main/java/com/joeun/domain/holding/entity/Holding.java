package com.joeun.domain.holding.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Holding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Todo: User 엔티티와 연관관계 설정
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // Todo: Item 엔티티와 연관관계 설정
    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private HoldingStatus status;

    @Column(name = "offered_at", nullable = false)
    private LocalDateTime offeredAt;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    @Builder
    public Holding(Long userId, Long itemId, HoldingStatus status, LocalDateTime offeredAt, LocalDateTime expiredAt) {
        this.userId = userId;
        this.itemId = itemId;
        this.status = status;
        this.offeredAt = offeredAt;
        this.expiredAt = expiredAt;
    }
}
