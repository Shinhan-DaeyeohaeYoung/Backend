package com.joeun.domain.statistics.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyItemRentalCount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(name = "item_name", nullable = false)
    private String itemName;

    @Column(name = "rental_count", nullable = false)
    private Integer rentalCount;

    @Column(name = "statistics_date", nullable = false)
    private LocalDate statisticsDate;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Builder
    public DailyItemRentalCount(Long itemId, String itemName, Integer rentalCount, LocalDate statisticsDate, Long organizationId) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.rentalCount = rentalCount;
        this.statisticsDate = statisticsDate;
        this.organizationId = organizationId;
    }
}
