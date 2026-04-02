package com.consignment.service.model.csa;

import java.time.Instant;
import java.util.List;

public record CsaResponse(
        String id,
        String docNo,
        String company,
        String store,
        String supplierCode,
        String supplierContract,
        String transactionType,
        String referenceNo,
        String reasonCode,
        String remark,
        String createFrom,
        String status,
        String createdBy,
        Instant releasedAt,
        String releasedBy,
        Instant createdAt,
        Instant updatedAt,
        List<CsaResponseDetail> items
) {
}
