package com.consignment.batch.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Report pre-computation job for heavy reports.
 * Triggers every night at 02:00 AM.
 */
@Component
public class ReportPreComputeJob {

    private static final Logger log = LoggerFactory.getLogger(ReportPreComputeJob.class);

    private final JobLauncher jobLauncher;
    private final Job reportJob;

    public ReportPreComputeJob(JobLauncher jobLauncher, Job reportJob) {
        this.jobLauncher = jobLauncher;
        this.reportJob = reportJob;
    }

    @Scheduled(cron = "${batch.report.cron:0 0 2 * * *}")
    public void runReportPreCompute() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addString("reportDate", LocalDate.now().minusDays(1).toString())
                    .addLong("runAt", System.currentTimeMillis())
                    .toJobParameters();
            JobExecution execution = jobLauncher.run(reportJob, params);
            log.info("Report pre-compute job completed with status: {}", execution.getStatus());
        } catch (Exception e) {
            log.error("Report pre-compute job failed: {}", e.getMessage(), e);
        }
    }
}
