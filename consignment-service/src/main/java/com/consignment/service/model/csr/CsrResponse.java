package com.consignment.service.model.csr;

import java.time.Instant;
import java.util.List;

public record CsrResponse(
        String id,
        String docNo,
        String company,
        String store,
        String supplierCode,
        String supplierContract,
        String internalSupplierStore,
        String supplierConfirmNote,
        String reasonCode,
        String remark,
        String status,
        String createdBy,
        String referenceNo,
        Instant releasedAt,
        Instant completedAt,
        Instant createdAt,
        Instant updatedAt,
        List<CsrResponseDetail> items
) {
}
