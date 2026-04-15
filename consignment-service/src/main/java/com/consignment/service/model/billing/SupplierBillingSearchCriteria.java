package com.consignment.service.model.billing;

import java.time.LocalDate;

public record SupplierBillingSearchCriteria(
        String docNo,
        String company,
        String store,
        String supplierCode,
        String supplierContract,
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
