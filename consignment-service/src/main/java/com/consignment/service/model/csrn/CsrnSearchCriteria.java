package com.consignment.service.model.csrn;

import java.time.LocalDate;

public record CsrnSearchCriteria(
        String docNo,
        String company,
        String store,
        String supplierCode,
        String csoDocNo,
        String status,
        String createdBy,
        String itemCode,
        LocalDate updatedFrom,
        LocalDate updatedTo,
        int page,
        int perPage
) {
    public int offset() { return (page - 1) * perPage; }
}
