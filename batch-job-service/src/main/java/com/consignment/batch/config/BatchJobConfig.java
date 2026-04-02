package com.consignment.batch.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BatchJobConfig {

    @Bean
    public Job settlementJob(JobRepository jobRepository,
                             @Qualifier("settlementStep") Step settlementStep) {
        return new JobBuilder("settlementJob", jobRepository)
                .start(settlementStep)
                .build();
    }

    @Bean
    public Job reportJob(JobRepository jobRepository,
                         @Qualifier("reportStep") Step reportStep) {
        return new JobBuilder("reportJob", jobRepository)
                .start(reportStep)
                .build();
    }

    @Bean
    @Qualifier("settlementStep")
    public Step settlementStep(JobRepository jobRepository, PlatformTransactionManager txManager) {
        return new StepBuilder("settlementStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    // TODO: implement settlement computation logic
                    return RepeatStatus.FINISHED;
                }, txManager)
                .build();
    }

    @Bean
    @Qualifier("reportStep")
    public Step reportStep(JobRepository jobRepository, PlatformTransactionManager txManager) {
        return new StepBuilder("reportStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    // TODO: implement report pre-computation logic
                    return RepeatStatus.FINISHED;
                }, txManager)
                .build();
    }
}
