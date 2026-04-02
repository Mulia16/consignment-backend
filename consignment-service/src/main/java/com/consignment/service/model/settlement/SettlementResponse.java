package com.consignment.service.model.settlement;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record SettlementResponse(
        String id,
        String docNo,
        String company,
        String store,
        String settlementType,
        String customerCode,
        String supplierCode,
        String supplierContract,
        BigDecimal totalAmount,
        String currency,
        String status,
        String createdBy,
        String referenceNo,
        String remark,
        Instant readyForBillingAt,
        Instant billedAt,
        Instant settledAt,
        Instant createdAt,
        Instant updatedAt,
        List<SettlementDetailResponse> details
) {}
