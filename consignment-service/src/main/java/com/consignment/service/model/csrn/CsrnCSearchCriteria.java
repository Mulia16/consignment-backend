package com.consignment.service.model.csrn;

import java.time.LocalDate;

public record CsrnCSearchCriteria(
        String docNo,
        String company,
        String store,
        String supplierCode,
        String supplierContract,
        String reasonCode,
        String createdBy,
        String csrnDocNo,
        LocalDate createdFrom,
        LocalDate createdTo,
        int page,
        int perPage
) {
    public int offset() { return (page - 1) * perPage; }
}
