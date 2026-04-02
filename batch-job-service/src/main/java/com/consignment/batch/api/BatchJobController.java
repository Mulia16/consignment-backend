package com.consignment.batch.api;

import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

/**
 * Manual trigger endpoint for batch jobs (admin use).
 */
@RestController
@RequestMapping("/batch")
public class BatchJobController {

    private final JobLauncher jobLauncher;
    private final Job settlementJob;
    private final Job reportJob;

    public BatchJobController(JobLauncher jobLauncher, Job settlementJob, Job reportJob) {
        this.jobLauncher = jobLauncher;
        this.settlementJob = settlementJob;
        this.reportJob = reportJob;
    }

    @PostMapping("/settlement/trigger")
    public ResponseEntity<?> triggerSettlement(@RequestParam(defaultValue = "") String businessDate) {
        return runJob(settlementJob, "businessDate",
                businessDate.isBlank() ? LocalDate.now().toString() : businessDate);
    }

    @PostMapping("/report/trigger")
    public ResponseEntity<?> triggerReport(@RequestParam(defaultValue = "") String reportDate) {
        return runJob(reportJob, "reportDate",
                reportDate.isBlank() ? LocalDate.now().minusDays(1).toString() : reportDate);
    }

    private ResponseEntity<?> runJob(Job job, String dateKey, String dateValue) {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addString(dateKey, dateValue)
                    .addLong("runAt", System.currentTimeMillis())
                    .toJobParameters();
            JobExecution execution = jobLauncher.run(job, params);
            return ResponseEntity.ok(Map.of("status", execution.getStatus().toString(),
                    "jobId", execution.getJobId()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
