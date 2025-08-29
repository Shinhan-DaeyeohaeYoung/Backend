package com.joeun.step;

import com.joeun.domain.rental.dto.RentalCountProjection;
import com.joeun.domain.statistics.entity.DailyItemOverdueCount;
import com.joeun.processor.DailyItemOverdueCountProcessor;
import com.joeun.reader.DailyItemRentalCountReader;
import com.joeun.writer.DailyItemOverdueCountWriter;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration("dailyItemOverdueCountStepConfig")
public class DailyItemOverdueCountStep extends DefaultBatchConfiguration {

    @Bean
    public Step dailyItemOverdueCountStep(
            JobRepository repository,
            PlatformTransactionManager transactionManager,
            DailyItemRentalCountReader dailyItemRentalCountReader,
            DailyItemOverdueCountProcessor dailyItemOverdueCountProcessor,
            DailyItemOverdueCountWriter dailyItemOverdueCountWriter) {
        return new StepBuilder("dailyItemOverdueCountStep", repository)
                .<RentalCountProjection, DailyItemOverdueCount>chunk(1000, transactionManager)
                .reader(dailyItemRentalCountReader)
                .processor(dailyItemOverdueCountProcessor)
                .writer(dailyItemOverdueCountWriter)
                .transactionManager(transactionManager)
                .build();
    }
}
