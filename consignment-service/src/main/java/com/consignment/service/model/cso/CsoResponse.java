package com.consignment.service.model.cso;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record CsoResponse(
        String id,
        String docNo,
        String company,
        String store,
        String customerCode,
        String customerBranch,
        String customerEmail,
        String supplierCode,
        String supplierContract,
        boolean autoGenerateCsdo,
        String note,
        String status,
        String createdBy,
        String createdMethod,
        String referenceNo,
        String shippingTerm,
        LocalDate deliveryDate,
        String shippingMode,
        String transporter,
        String shippingTo,
        String shippingAddress,
        String customerReference,
        String transportInformation,
        Instant releasedAt,
        String releasedBy,
        Instant createdAt,
        Instant updatedAt,
        List<CsoResponseDetail> items
) {
}
