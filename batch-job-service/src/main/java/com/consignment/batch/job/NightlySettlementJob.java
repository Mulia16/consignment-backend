package com.consignment.batch.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;

/**
 * Nightly settlement computation job.
 * Triggers every night at 01:00 AM.
 */
@Component
public class NightlySettlementJob {

    private static final Logger log = LoggerFactory.getLogger(NightlySettlementJob.class);

    private final JobLauncher jobLauncher;
    private final Job settlementJob;

    public NightlySettlementJob(JobLauncher jobLauncher, Job settlementJob) {
        this.jobLauncher = jobLauncher;
        this.settlementJob = settlementJob;
    }

    @Scheduled(cron = "${batch.settlement.cron:0 0 1 * * *}")
    public void runSettlement() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addString("businessDate", LocalDate.now().toString())
                    .addLong("runAt", System.currentTimeMillis())
                    .toJobParameters();
            JobExecution execution = jobLauncher.run(settlementJob, params);
            log.info("Settlement job completed with status: {}", execution.getStatus());
        } catch (Exception e) {
            log.error("Settlement job failed: {}", e.getMessage(), e);
        }
    }
}
