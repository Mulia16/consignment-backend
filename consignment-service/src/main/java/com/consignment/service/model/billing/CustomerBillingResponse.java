package com.consignment.service.model.billing;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record CustomerBillingResponse(
        String id,
        String docNo,
        String periodType,
        LocalDate fromDate,
        LocalDate toDate,
        String store,
        String customerCode,
        String customerBranch,
        String status,
        String processStatus,
        String createdBy,
        Instant releasedAt,
        Instant createdAt,
        Instant updatedAt,
        List<CustomerBillingDetailResponse> details
) {}
