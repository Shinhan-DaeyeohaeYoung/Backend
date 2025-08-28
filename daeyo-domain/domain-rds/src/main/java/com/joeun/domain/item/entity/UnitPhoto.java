package com.joeun.domain.item.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.*;

@Entity
@Table(
        name = "unit_photo",
        uniqueConstraints = @UniqueConstraint(name = "uk_unit_photo_unit", columnNames = {"individual_item_id"}),
        indexes = {
                @Index(name = "idx_unit_photo_unit", columnList = "individual_item_id"),
                @Index(name = "idx_unit_photo_univ_org", columnList = "university_id, organization_id")
        }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE)
@Builder(toBuilder = true)
public class UnitPhoto {

    @Id @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(name = "university_id", nullable = false)
    private Long universityId;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    /** 유닛(개별 자산)과 1:1 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "individual_item_id", nullable = false)
    private IndividualItem unit;

    /** S3 object key */
    @Column(name = "image_key", length = 512, nullable = false)
    private String imageKey;

    @Column(name = "mime", length = 64)
    private String mime;

    /** SHA-256 등 무결성 해시(옵션) */
    @Column(name = "hash", length = 64)
    private String hash;

    @Column(name = "taken_at")
    private LocalDateTime takenAt;

    @Column(name = "caption")
    private String caption;

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /* ===== 도메인 메서드 ===== */
    public void replace(String key, String mime, String hash, LocalDateTime takenAt) {
        this.imageKey = key;
        this.mime = mime;
        this.hash = hash;
        this.takenAt = takenAt;
    }
    public void replacePhoto(String newKey, String newMime, String newHash) {
        this.imageKey = newKey;
        if (newMime != null && !newMime.isBlank()) this.mime = newMime;
        if (newHash != null && !newHash.isBlank()) this.hash = newHash;
    }

}
