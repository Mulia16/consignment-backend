package com.consignment.service.model.csrv;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record CsrvResponse(
        String id,
        String docNo,
        String company,
        String receivingStore,
        String supplierCode,
        String supplierContract,
        String branch,
        String supplierDoNo,
        LocalDate deliveryDate,
        String remark,
        String status,
        String createdBy,
        String createdMethod,
        String referenceNo,
        Instant releasedAt,
        Instant createdAt,
        Instant updatedAt,
        List<CsrvResponseDetail> items
) {
}
