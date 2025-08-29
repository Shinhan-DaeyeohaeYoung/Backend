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
public class DailyItemOverdueCount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(name = "item_name", nullable = false)
    private String itemName;

    @Column(name = "overdue_count", nullable = false)
    private Integer overdueCount;

    @Column(name = "statistics_date", nullable = false)
    private LocalDate statisticsDate;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Builder
    public DailyItemOverdueCount(Long itemId, String itemName, Integer overdueCount, LocalDate statisticsDate, Long organizationId) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.overdueCount = overdueCount;
        this.statisticsDate = statisticsDate;
        this.organizationId = organizationId;
    }
}
