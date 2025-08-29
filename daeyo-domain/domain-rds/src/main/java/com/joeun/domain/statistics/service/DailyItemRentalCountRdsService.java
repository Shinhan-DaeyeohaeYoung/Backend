package com.joeun.domain.statistics.service;

import com.joeun.domain.statistics.entity.DailyItemRentalCount;
import com.joeun.domain.statistics.repository.DailyItemRentalCountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DailyItemRentalCountRdsService {

    private final DailyItemRentalCountRepository dailyItemRentalCountRepository;

    // 조직별 일별 대여 아이템 통계 조회
    @Transactional(readOnly = true)
    public List<DailyItemRentalCount> findAllByOrganizationIdAndStatisticsDate(Long organizationId, LocalDate statisticsDate) {
        return dailyItemRentalCountRepository.findAllByOrganizationIdAndStatisticsDate(organizationId, statisticsDate);
    }

    // 조직별 기간별 대여 아이템 통계 조회
    @Transactional(readOnly = true)
    public List<DailyItemRentalCount> findByOrganizationIdAndStatisticsDateBetween(Long organizationId, LocalDate startDate, LocalDate endDate) {
        return dailyItemRentalCountRepository.findByOrganizationIdAndStatisticsDateBetween(organizationId, startDate, endDate);
    }
}
