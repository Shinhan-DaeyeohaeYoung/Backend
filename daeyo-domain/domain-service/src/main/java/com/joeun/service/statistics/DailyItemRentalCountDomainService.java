package com.joeun.service.statistics;

import com.joeun.domain.statistics.entity.DailyItemRentalCount;
import com.joeun.domain.statistics.service.DailyItemRentalCountRdsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DailyItemRentalCountDomainService {

    private final DailyItemRentalCountRdsService dailyItemRentalCountRdsService;

    public List<DailyItemRentalCount> getDailyItemRentalCount(Long organizationId, String statisticsDate) {
        return dailyItemRentalCountRdsService.findAllByOrganizationIdAndStatisticsDate(organizationId, java.time.LocalDate.parse(statisticsDate));
    }

    public List<DailyItemRentalCount> getDailyItemRentalCounts(Long organizationId, LocalDate startDate, LocalDate endDate) {
        return dailyItemRentalCountRdsService.findByOrganizationIdAndStatisticsDateBetween(organizationId, startDate, endDate);
    }
}
