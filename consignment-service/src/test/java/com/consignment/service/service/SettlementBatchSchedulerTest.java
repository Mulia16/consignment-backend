package com.consignment.service.service;

import com.consignment.service.config.SettlementBatchProperties;
import com.consignment.service.model.settlement.SettlementBatchGenerateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class SettlementBatchSchedulerTest {

    @Mock
    private SettlementService settlementService;

    private SettlementBatchProperties properties;
    private SettlementBatchScheduler scheduler;

    @BeforeEach
    void setUp() {
        properties = new SettlementBatchProperties();
        scheduler = new SettlementBatchScheduler(settlementService, properties);
    }

    @Test
    void shouldSkipWeeklyRunWhenSchedulerDisabled() {
        properties.setEnabled(false);

        scheduler.runWeekly();

        verifyNoInteractions(settlementService);
    }

    @Test
    void shouldGenerateWeeklyBatchFromConfiguredProfile() {
        properties.setEnabled(true);
        properties.setTimezone("UTC");
        properties.setProfiles(List.of(profile("Weekly Supplier", "SUPPLIER")));

        scheduler.runWeekly();

        ArgumentCaptor<SettlementBatchGenerateRequest> requestCaptor = ArgumentCaptor.forClass(SettlementBatchGenerateRequest.class);
        verify(settlementService, times(1)).generateBatch(requestCaptor.capture());

        SettlementBatchGenerateRequest request = requestCaptor.getValue();
        LocalDate today = LocalDate.now(ZoneId.of("UTC"));
        assertEquals(today.minusDays(7), request.fromDate());
        assertEquals(today.minusDays(1), request.toDate());
        assertEquals("COMP01", request.company());
        assertEquals("STORE01", request.store());
        assertEquals("SUPPLIER", request.settlementType());
        assertEquals("SUPP01", request.supplierCode());
        assertEquals("CONTRACT01", request.supplierContract());
        assertEquals(new BigDecimal("12.50"), request.defaultUnitPrice());
        assertEquals("WEEKLY-" + request.fromDate() + "-" + request.toDate() + "-WeeklySupplier", request.referenceNo());
    }

    @Test
    void shouldGenerateMonthlyBatchFromConfiguredProfile() {
        properties.setEnabled(true);
        properties.setTimezone("UTC");
        properties.setProfiles(List.of(profile("Monthly Customer", "CUSTOMER")));

        scheduler.runMonthly();

        ArgumentCaptor<SettlementBatchGenerateRequest> requestCaptor = ArgumentCaptor.forClass(SettlementBatchGenerateRequest.class);
        verify(settlementService, times(1)).generateBatch(requestCaptor.capture());

        SettlementBatchGenerateRequest request = requestCaptor.getValue();
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
        LocalDate firstOfCurrent = now.toLocalDate().withDayOfMonth(1);
        LocalDate lastOfPrevious = firstOfCurrent.minusDays(1);
        LocalDate firstOfPrevious = lastOfPrevious.withDayOfMonth(1);

        assertEquals(firstOfPrevious, request.fromDate());
        assertEquals(lastOfPrevious, request.toDate());
        assertEquals("CUSTOMER", request.settlementType());
        assertEquals("CUST01", request.customerCode());
        assertEquals("MONTHLY-" + firstOfPrevious + "-" + lastOfPrevious + "-MonthlyCustomer", request.referenceNo());
    }

    private SettlementBatchProperties.Profile profile(String name, String settlementType) {
        SettlementBatchProperties.Profile profile = new SettlementBatchProperties.Profile();
        profile.setName(name);
        profile.setCompany("COMP01");
        profile.setStore("STORE01");
        profile.setSettlementType(settlementType);
        profile.setCustomerCode("CUSTOMER".equals(settlementType) ? "CUST01" : null);
        profile.setSupplierCode("SUPPLIER".equals(settlementType) ? "SUPP01" : null);
        profile.setSupplierContract("SUPPLIER".equals(settlementType) ? "CONTRACT01" : null);
        profile.setCurrency("IDR");
        profile.setCreatedBy("batch-user");
        profile.setDefaultUnitPrice(new BigDecimal("12.50"));
        return profile;
    }
}