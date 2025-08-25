package com.joeun.domain.returnrequest.entity;

import com.joeun.domain.rental.entity.Rental;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "return_request",
        indexes = {
                @Index(name = "ix_return_req_rental", columnList = "rental_id"),
                @Index(name = "ix_return_req_univ_org", columnList = "university_id, organization_id"),
                @Index(name = "ix_return_req_user", columnList = "user_id"),
                @Index(name = "ix_return_req_rental_active", columnList = "rental_id, is_active")

        }
        // '열린 신청 1건 제약'은 DB 부분 유니크(부분 인덱스)가 어려우니 서비스에서 검증/락으로 보장할 예정
)
public class ReturnRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 테넌트 */
    @Column(name = "university_id", nullable = false)
    private Long universityId;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    /** 어떤 대여에 대한 반납신청인지 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rental_id", nullable = false)
    private Rental rental;

    /** 신청자(유저) */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 승인 관리자(옵션) */
    @Column(name = "approver_user_id")
    private Long approverUserId;

    /** 상태 */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 16, nullable = false)
    private ReturnRequestStatus status;

    /** 타임스탬프들 */
    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    /** 제출 이미지 메타 */
    @Column(name = "submitted_image_key", length = 512)
    private String submittedImageKey;

    @Column(name = "submitted_image_mime", length = 64)
    private String submittedImageMime;

    @Column(name = "submitted_image_hash", length = 64)
    private String submittedImageHash;

    @Column(name = "submitted_image_taken_at")
    private LocalDateTime submittedImageTakenAt;

    /** 열린 신청 */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    /** 생성/수정 시각 */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


    /* ====== 팩토리/도메인 메서드 ====== */

    public static ReturnRequest create(Long universityId,
                                       Long organizationId,
                                       Long userId,
                                       Rental rental,
                                       String imageKey,
                                       String imageMime,
                                       String imageHash,
                                       LocalDateTime imageTakenAt,
                                       LocalDateTime now) {
        return ReturnRequest.builder()
                .universityId(universityId)
                .organizationId(organizationId)
                .userId(userId)
                .rental(rental)
                .status(ReturnRequestStatus.REQUESTED)
                .requestedAt(now == null ? LocalDateTime.now() : now)
                .submittedImageKey(imageKey)
                .submittedImageMime(imageMime)
                .submittedImageHash(imageHash)
                .submittedImageTakenAt(imageTakenAt)
                .isActive(true)
                .build();
    }

    public void approve(Long approverUserId, LocalDateTime now) {
        if (this.status != ReturnRequestStatus.REQUESTED) {
            throw new IllegalStateException("ReturnRequest is not REQUESTED");
        }
        this.status = ReturnRequestStatus.APPROVED;
        this.approverUserId = approverUserId;
        this.approvedAt = now == null ? LocalDateTime.now() : now;
        this.isActive = false; // 열린 신청 종료
    }

    public void reject(Long approverUserId, LocalDateTime now) {
        if (this.status != ReturnRequestStatus.REQUESTED) {
            throw new IllegalStateException("ReturnRequest is not REQUESTED");
        }
        this.status = ReturnRequestStatus.REJECTED;
        this.approverUserId = approverUserId;
        this.rejectedAt = now == null ? LocalDateTime.now() : now;
        this.isActive = false;
    }

    public void cancelByUser(Long actorUserId) {
        if (!this.userId.equals(actorUserId)) throw new IllegalStateException("not owner");
        if (status != ReturnRequestStatus.REQUESTED) throw new IllegalStateException("not REQUESTED");
        this.status = ReturnRequestStatus.CANCELLED;
        this.isActive = false;
    }

    public void replaceSubmittedImage(String key, String mime, String hash, LocalDateTime takenAt) {
        this.submittedImageKey = key;
        this.submittedImageMime = mime;
        this.submittedImageHash = hash;
        this.submittedImageTakenAt = takenAt;
    }
}
