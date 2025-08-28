package com.joeun.domain.holding.entity;

import com.joeun.domain.item.entity.IndividualItem;
import com.joeun.domain.item.entity.Item;
import com.joeun.domain.users.entity.User;
import com.joeun.domain.waitlist.entity.Waitlist;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Entity
@Table(
        name = "holding",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_unit_active", columnNames = {"unit_id", "active"}),
                @UniqueConstraint(name = "uk_holding_offer_token", columnNames = {"offer_token"})
        },
        indexes = {
                @Index(name = "idx_user_status", columnList = "user_id, status"),
                @Index(name = "idx_item_status", columnList = "item_id, status")
        }
)
public class Holding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "unit_id", nullable = false)
    private IndividualItem unit;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "waitlist_id", nullable = false)
    private Waitlist waitlist;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private HoldingStatus status;

    @Column(name = "notified_at", nullable = false)
    private LocalDateTime notifiedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "offer_token", nullable = false, length = 36)
    private String offerToken;

    @Column(name = "active", nullable = false, insertable = false, updatable = false)
    private Boolean active;

    public static Holding offerOf(
            Item item,
            IndividualItem unit,
            User user,
            Waitlist waitlist,
            LocalDateTime notifiedAt,
            LocalDateTime expiresAt,
            String offerToken
    ) {
        return Holding.builder()
                .item(item)
                .unit(unit)
                .user(user)
                .waitlist(waitlist)
                .status(HoldingStatus.OFFERED)
                .notifiedAt(notifiedAt)
                .expiresAt(expiresAt)
                .offerToken(offerToken)
                .build();
    }

    public boolean isExpired(LocalDateTime now) {
        return now.isAfter(expiresAt) || now.isEqual(expiresAt);
    }

    public void markAccepted() {
        if (this.status != HoldingStatus.OFFERED) {
            throw new IllegalStateException("Only OFFERED can be accepted");
        }
        this.status = HoldingStatus.ACCEPTED;
    }

    public void markExpired() {
        if (this.status == HoldingStatus.ACCEPTED || this.status == HoldingStatus.CANCELLED) {
            throw new IllegalStateException("Cannot expire accepted/cancelled holding");
        }
        this.status = HoldingStatus.EXPIRED;
    }

    public void cancel() {
        if (this.status == HoldingStatus.ACCEPTED) {
            throw new IllegalStateException("Cannot cancel accepted holding");
        }
        this.status = HoldingStatus.CANCELLED;
    }

    @PrePersist
    private void onCreate() {
        if (this.status == null) this.status = HoldingStatus.OFFERED;
    }

}
