package com.joeun.processor;

import com.joeun.domain.rental.dto.RentalCountProjection;
import com.joeun.domain.statistics.entity.DailyItemRentalCount;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@StepScope
public class DailyItemRentalCountProcessor
        implements ItemProcessor<RentalCountProjection, DailyItemRentalCount> {

    private final LocalDate statDate;

    public DailyItemRentalCountProcessor(
            @Value("#{T(java.time.LocalDate).parse(jobParameters['statDate'])}") LocalDate statDate) {
        this.statDate = statDate;
    }

    @Override
    public DailyItemRentalCount process(RentalCountProjection projection) {
        return DailyItemRentalCount.builder()
                .itemId(projection.getItemId())
                .itemName(projection.getItemName())
                .rentalCount(projection.getRentalCount())
                .statisticsDate(statDate)
                .organizationId(projection.getOrganizationId())
                .build();
    }
}

