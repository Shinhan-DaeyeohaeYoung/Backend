package com.joeun.domain.item.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(name = "individual_item",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_individual_item_item_assetno",
                columnNames = {"item_id", "asset_no"}
        ))
@Getter
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PRIVATE) @Builder(toBuilder = true)
public class IndividualItem {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY) @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private IndividualItemStatus status;

    @Column(name = "asset_no", nullable = false, length = 64)
    private String assetNo;
    @Lob private String description;

    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;

    public void changeStatus(IndividualItemStatus newStatus) {
        this.status = newStatus;
    }
}

