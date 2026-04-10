package com.consignment.service.model.csrn;

import java.time.Instant;
import java.util.List;

public record CsrnResponse(
        String id,
        String docNo,
        String csoDocNo,
        String company,
        String store,
        String supplierCode,
        String supplierContract,
        String internalSupplierStore,
        String reasonCode,
        String remark,
        String status,
        String createdBy,
        String referenceNo,
        Instant createdAt,
        Instant updatedAt,
        List<CsrnResponseDetail> items
) {
}
