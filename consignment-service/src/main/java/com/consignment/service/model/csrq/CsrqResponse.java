package com.consignment.service.model.csrq;

import java.time.Instant;
import java.util.List;

public record CsrqResponse(
        String id,
        String docNo,
        String company,
        String store,
        String supplierCode,
        String supplierContract,
        String branch,
        String internalSupplierStore,
        String notes,
        String status,
        String createdBy,
        String createdMethod,
        String referenceNo,
        Instant releasedAt,
        Instant createdAt,
        Instant updatedAt,
        List<CsrqResponseDetail> items
) {
}
