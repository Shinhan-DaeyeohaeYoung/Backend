package com.joeun.step;

import com.joeun.domain.rental.dto.RentalCountProjection;
import com.joeun.domain.statistics.entity.DailyItemRentalCount;
import com.joeun.processor.DailyItemRentalCountProcessor;
import com.joeun.reader.DailyItemRentalCountReader;
import com.joeun.writer.DailyItemRentalCountWriter;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration("dailyItemRentalCountStepConfig")
public class DailyItemRentalCountStep extends DefaultBatchConfiguration {

    @Bean
    public Step dailyItemRentalCountStep(
            JobRepository repository,
            PlatformTransactionManager transactionManager,
            DailyItemRentalCountReader dailyItemRentalCountReader,
            DailyItemRentalCountProcessor dailyItemRentalCountProcessor,
            DailyItemRentalCountWriter dailyItemRentalCountWriter) {
        return new StepBuilder("dailyItemRentalCountStep", repository)
                .<RentalCountProjection, DailyItemRentalCount>chunk(1000, transactionManager)
                .reader(dailyItemRentalCountReader)
                .processor(dailyItemRentalCountProcessor)
                .writer(dailyItemRentalCountWriter)
                .transactionManager(transactionManager)
                .build();
    }
}
