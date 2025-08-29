package com.joeun.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class DailyItemRentalCountScheduler {

    private final JobLauncher jobLauncher;

    @Qualifier("dailyItemRentalStatisticsJob")
    private final Job dailyItemRentalCountJob;

    @Scheduled(cron = "0 10 0 * * ?")
    public void runDailyItemRentalCountJob() {
        JobParameters params =
                new JobParametersBuilder()
                        .addString("statDate",
                                LocalDate.now()
                                        .minusDays(1)
                                        .format(DateTimeFormatter.ISO_LOCAL_DATE))
                        .addLong("run.id", System.currentTimeMillis())
                        .toJobParameters();
        try {
            jobLauncher.run(dailyItemRentalCountJob, params);
        } catch (Exception e) {
            log.error("Daily item rental count job failed: {}", e.getMessage(), e);
            throw new RuntimeException("Daily item rental count job execution failed", e);
        }
    }
}
