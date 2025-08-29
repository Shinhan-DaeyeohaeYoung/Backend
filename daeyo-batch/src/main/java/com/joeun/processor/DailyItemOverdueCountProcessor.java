package com.joeun.processor;

import com.joeun.domain.rental.dto.RentalCountProjection;
import com.joeun.domain.statistics.entity.DailyItemOverdueCount;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@StepScope
public class DailyItemOverdueCountProcessor implements ItemProcessor<RentalCountProjection, DailyItemOverdueCount> {

    private final LocalDate statDate;

    public DailyItemOverdueCountProcessor(
            @Value("#{T(java.time.LocalDate).parse(jobParameters['statDate'])}") LocalDate statDate) {
        this.statDate = statDate;
    }

    @Override
    public DailyItemOverdueCount process(RentalCountProjection item) throws Exception {
        if(item.getOverdueCount() == 0){
            return null;
        }

        return DailyItemOverdueCount.builder()
                .itemId(item.getItemId())
                .itemName(item.getItemName())
                .overdueCount(item.getOverdueCount())
                .statisticsDate(statDate)
                .organizationId(item.getOrganizationId())
                .build();
    }
}
