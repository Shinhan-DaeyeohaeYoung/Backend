package com.joeun.job;

import com.joeun.partitioner.OrganzationIdPartitioner;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration("dailyItemRentalStatisticsJobConfig")
public class DailyItemRentalStatisticsJob {

    @Bean
    public Job dailyItemRentalStatisticsJob(
            JobRepository jobRepository,
            @Qualifier("partitionedDailyItemRentalCountStep") Step partitionedStep,
            @Qualifier("partitionedDailyItemOverdueCountStep") Step partitionedOverdueStep) {
        return new JobBuilder("dailyItemRentalStatisticsJob", jobRepository)
                .start(partitionedStep)
                .next(partitionedOverdueStep)
                .build();
    }

    @Bean(name = "partitionedDailyItemRentalCountStep")
    public Step partitionedDailyItemRentalCountStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            @Qualifier("dailyTaskExecutor") TaskExecutor dailyTaskExecutor,
            OrganzationIdPartitioner organzationIdPartitioner,
            @Qualifier("dailyItemRentalCountStep") Step workerStep,
            @Value("${batch.partition.grid-size:4}") int gridSize) {
        return new StepBuilder("partitionedDailyItemRentalCountStep", jobRepository)
                .partitioner("dailyItemRentalCountStep", organzationIdPartitioner)
                .step(workerStep)
                .gridSize(gridSize)
                .taskExecutor(dailyTaskExecutor)
                .build();
    }

    @Bean(name = "partitionedDailyItemOverdueCountStep")
    public Step partitionedDailyItemOverdueCountStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            @Qualifier("dailyTaskExecutor") TaskExecutor dailyTaskExecutor,
            OrganzationIdPartitioner organzationIdPartitioner,
            @Qualifier("dailyItemOverdueCountStep") Step workerStep,
            @Value("${batch.partition.grid-size:4}") int gridSize) {
        return new StepBuilder("partitionedDailyItemOverdueCountStep", jobRepository)
                .partitioner("dailyItemOverdueCountStep", organzationIdPartitioner)
                .step(workerStep)
                .gridSize(gridSize)
                .taskExecutor(dailyTaskExecutor)
                .build();
    }

    @Bean
    public TaskExecutor dailyTaskExecutor(
            @Value("${batch.partition.threads:4}") int threads,
            @Value("${batch.partition.queue-capacity:100}") int queueCapacity) {
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setCorePoolSize(threads);
        ex.setMaxPoolSize(threads);
        ex.setQueueCapacity(queueCapacity);
        ex.setThreadNamePrefix("daily-");
        ex.setRejectedExecutionHandler(
                new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        ex.initialize();
        return ex;
    }
}
