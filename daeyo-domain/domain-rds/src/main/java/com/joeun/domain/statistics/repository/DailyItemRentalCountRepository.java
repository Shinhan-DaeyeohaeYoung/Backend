package com.joeun.domain.statistics.repository;

import com.joeun.domain.statistics.entity.DailyItemRentalCount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyItemRentalCountRepository extends JpaRepository<DailyItemRentalCount, Long> {
    List<DailyItemRentalCount> findAllByOrganizationIdAndStatisticsDate(Long organizationId, LocalDate statisticsDate);

    List<DailyItemRentalCount> findByOrganizationIdAndStatisticsDateBetween(Long organizationId, LocalDate startDate, LocalDate endDate);
}
