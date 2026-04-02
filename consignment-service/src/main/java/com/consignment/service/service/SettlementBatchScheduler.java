package com.consignment.service.service;

import com.consignment.service.config.SettlementBatchProperties;
import com.consignment.service.exception.BusinessRuleViolationException;
import com.consignment.service.model.settlement.SettlementBatchGenerateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
public class SettlementBatchScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SettlementBatchScheduler.class);

    private final SettlementService settlementService;
    private final SettlementBatchProperties properties;

    public SettlementBatchScheduler(SettlementService settlementService, SettlementBatchProperties properties) {
        this.settlementService = settlementService;
        this.properties = properties;
    }

    @Scheduled(cron = "${app.settlement.batch.weekly-cron:0 0 2 * * MON}", zone = "${app.settlement.batch.timezone:UTC}")
    public void runWeekly() {
        if (!properties.isEnabled() || properties.getProfiles().isEmpty()) {
            return;
        }
        ZoneId zoneId = ZoneId.of(properties.getTimezone());
        LocalDate today = LocalDate.now(zoneId);
        LocalDate fromDate = today.minusDays(7);
        LocalDate toDate = today.minusDays(1);
        triggerProfiles("WEEKLY", fromDate, toDate);
    }

    @Scheduled(cron = "${app.settlement.batch.monthly-cron:0 0 3 1 * *}", zone = "${app.settlement.batch.timezone:UTC}")
    public void runMonthly() {
        if (!properties.isEnabled() || properties.getProfiles().isEmpty()) {
            return;
        }
        ZoneId zoneId = ZoneId.of(properties.getTimezone());
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        LocalDate firstOfCurrent = now.toLocalDate().withDayOfMonth(1);
        LocalDate lastOfPrevious = firstOfCurrent.minusDays(1);
        LocalDate firstOfPrevious = lastOfPrevious.withDayOfMonth(1);
        triggerProfiles("MONTHLY", firstOfPrevious, lastOfPrevious);
    }

    private void triggerProfiles(String mode, LocalDate fromDate, LocalDate toDate) {
        for (SettlementBatchProperties.Profile profile : properties.getProfiles()) {
            String referenceNo = buildReference(mode, profile, fromDate, toDate);
            try {
                settlementService.generateBatch(new SettlementBatchGenerateRequest(
                        profile.getCompany(),
                        profile.getStore(),
                        profile.getSettlementType(),
                        profile.getCustomerCode(),
                        profile.getSupplierCode(),
                        profile.getSupplierContract(),
                        profile.getCurrency(),
                        profile.getCreatedBy(),
                        fromDate,
                        toDate,
                        profile.getDefaultUnitPrice(),
                        referenceNo
                ));
            } catch (BusinessRuleViolationException ex) {
                LOGGER.info("Skip {} settlement batch for profile {}: {}", mode, profile.getName(), ex.getMessage());
            } catch (Exception ex) {
                LOGGER.error("Failed {} settlement batch for profile {}", mode, profile.getName(), ex);
            }
        }
    }

    private String buildReference(String mode, SettlementBatchProperties.Profile profile, LocalDate fromDate, LocalDate toDate) {
        String profileName = profile.getName() == null || profile.getName().isBlank()
                ? profile.getSettlementType() + "-" + profile.getCompany() + "-" + profile.getStore()
                : profile.getName();
        String cleaned = profileName.replaceAll("[^A-Za-z0-9_-]", "");
        return mode + "-" + fromDate + "-" + toDate + "-" + cleaned;
    }
}