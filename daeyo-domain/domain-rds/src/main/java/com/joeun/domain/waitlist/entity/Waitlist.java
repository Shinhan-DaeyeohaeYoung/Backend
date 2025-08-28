package com.joeun.domain.waitlist.entity;

import com.joeun.domain.item.entity.Item;
import com.joeun.domain.users.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Waitlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Todo: 추 후 University Entity 생성 후 ManyToOne 매핑
    @Column(name = "university_id", nullable = false)
    private Long universityId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "priority", nullable = false)
    private Integer priority;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private WaitlistStatus status;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @Column(name = "offered_at")
    private LocalDateTime offeredAt;

    @Column(name = "offer_expires_at")
    private LocalDateTime offerExpiresAt;

    @Column(name = "notified_at")
    private LocalDateTime notifiedAt;

    @Builder
    public Waitlist(Long universityId, Item item, User user, Integer priority, WaitlistStatus status, LocalDateTime joinedAt) {
        this.universityId = universityId;
        this.item = item;
        this.user = user;
        this.priority = priority;
        this.status = status;
        this.joinedAt = joinedAt;
    }

    public void markNotified(LocalDateTime now) {
        this.status = WaitlistStatus.NOTIFIED;
        this.notifiedAt = now;
    }

    public void offer(LocalDateTime offeredAt) {
        this.status = WaitlistStatus.OFFERED;
        this.offeredAt = offeredAt;
        this.offerExpiresAt = offeredAt.plusMinutes(30);
    }

    public void markFulfilled() {
        this.status = WaitlistStatus.FULFILLED;
    }
}
