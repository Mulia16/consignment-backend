package com.consignment.service.model.report;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record ReportRow(
        String docNo,
        String documentType,
        String company,
        String store,
        String supplierCode,
        String supplierContract,
        String customerCode,
        String customerBranch,
        String itemCode,
        BigDecimal qty,
        BigDecimal unitPrice,
        BigDecimal lineAmount,
        String uom,
        String status,
        String referenceNo,
        LocalDate businessDate,
        Instant createdAt
) {}
