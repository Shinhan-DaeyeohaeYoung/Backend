package com.joeun.domain.rental.entity;

import com.joeun.domain.item.entity.IndividualItem;
import com.joeun.domain.item.entity.Item;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(
        name = "rental",
        indexes = {
                @Index(name = "idx_rental_univ_user_status", columnList = "university_id,user_id,status"),
                @Index(name = "idx_rental_item_status", columnList = "item_id,status"),
                @Index(name = "idx_rental_individual_item_id", columnList = "individual_item_id")
        }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
@Builder(toBuilder = true)
public class Rental {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    /** 멀티 테넌시 경계 */
    @Column(name = "university_id", nullable = false)
    private Long universityId;

    @Column(name = "organization_id")
    private Long organizationId;

    /** 누가 빌렸는지 (User 엔티티 연동 전이므로 FK 대신 id만 보유) */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 어떤 카탈로그 아이템인지 */
    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    /** 어떤 개별자산(유닛)인지 (단건 대여 기준) */
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "individual_item_id")
    private IndividualItem unit;

    /** 수량(단건 유닛 대여면 1) */
    @Column(nullable = false)
    private Integer quantity;

    // === 예약 단계 전용 필드 ===
    @Column(name = "reserved_at")
    private LocalDateTime reservedAt;

    @Column(name = "reserve_expires_at")
    private LocalDateTime reserveExpiresAt;

    /** 기간/상태 */
    @Column(name = "rented_at")
    private LocalDateTime rentedAt;

    @Column(name = "due_at")
    private LocalDateTime dueAt;

    @Column(name = "returned_at")
    private LocalDateTime returnedAt;

    @Enumerated(EnumType.STRING)
    @Column(length = 16, nullable = false)
    private RentalStatus status;

    /** 보증금 레코드 id (Deposit 엔티티 연동 전이라 id만) */
    @Column(name = "deposit_id")
    private Long depositId;

    /* ========= 도메인 메서드 ========= */

    /** 대여 시작 */
    public void markReserved(LocalDateTime now, LocalDateTime expiresAt) {
        this.status = RentalStatus.RESERVED;
        this.reservedAt = now;
        this.reserveExpiresAt = expiresAt;
        if (this.quantity == null) this.quantity = 1;
    }
    public boolean isReservationExpired(LocalDateTime now) {
        return reserveExpiresAt != null && now.isAfter(reserveExpiresAt);
    }
    public void confirmRental(LocalDateTime now, LocalDateTime due) {
        this.status = RentalStatus.RENTED;
        this.rentedAt = now;
        this.dueAt = due;
        // 예약 만료 필드는 선택적으로 비워도 됨
        // this.reserveExpiresAt = null;
    }
    public void cancelReservation() { this.status = RentalStatus.CANCELLED; }
}
