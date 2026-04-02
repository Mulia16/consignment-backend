package com.consignment.service.model.csdo;

import java.time.Instant;
import java.util.List;

public record CsdoResponse(
        String id,
        String docNo,
        String csoId,
        String csoDocNo,
        String company,
        String store,
        String customerCode,
        String customerBranch,
        String customerEmail,
        boolean requireGenerateCdo,
        String shippingMode,
        String transporter,
        String status,
        String createdBy,
        Instant releasedAt,
        Instant createdAt,
        Instant updatedAt,
        List<CsdoResponseDetail> items
) {
}
