package com.joeun.api.statistics.service;

import com.joeun.api.statistics.dto.ItemRentalCountResponse;
import com.joeun.domain.statistics.entity.DailyItemRentalCount;
import com.joeun.service.statistics.DailyItemRentalCountDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final DailyItemRentalCountDomainService dailyItemRentalCountDomainService;

    public List<ItemRentalCountResponse> getItemRentalCounts(Long organizationId, String statisticsDate) {
        return dailyItemRentalCountDomainService.getDailyItemRentalCount(organizationId, statisticsDate)
                .stream()
                .map(ItemRentalCountResponse::from)
                .toList();
    }

    public List<ItemRentalCountResponse> getItemRentalCounts(Long organizationId, LocalDate startDate, LocalDate endDate) {
        return new ArrayList<>(dailyItemRentalCountDomainService.getDailyItemRentalCounts(organizationId, startDate, endDate)
                .stream()
                .collect(Collectors.toMap(
                        DailyItemRentalCount::getItemId,
                        daily -> {
                            long count = daily.getRentalCount() == null ? 0L : daily.getRentalCount();
                            return ItemRentalCountResponse.builder()
                                    .itemId(daily.getItemId())
                                    .itemName(daily.getItemName())
                                    .rentalCount(count)
                                    .build();
                        },
                        (resp1, resp2) -> ItemRentalCountResponse.builder()
                                .itemId(resp1.itemId())
                                .itemName(resp1.itemName())
                                .rentalCount(resp1.rentalCount() + resp2.rentalCount())
                                .build(),
                        LinkedHashMap::new
                ))
                .values());
    }
}
