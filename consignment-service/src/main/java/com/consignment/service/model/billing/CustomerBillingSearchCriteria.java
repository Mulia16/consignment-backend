package com.consignment.service.model.billing;

import java.time.LocalDate;

public record CustomerBillingSearchCriteria(
        String docNo,
        String store,
        String customerCode,
        String customerBranch,
        String periodType,
        String status,
        String processStatus,
        LocalDate fromDate,
        LocalDate toDate,
        int page,
        int perPage
) {
    public int offset() { return (page - 1) * perPage; }
}
