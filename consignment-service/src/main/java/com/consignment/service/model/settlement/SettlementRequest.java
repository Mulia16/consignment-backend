package com.consignment.service.model.settlement;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record SettlementRequest(
        String company,
        String store,
        String settlementType,
        String customerCode,
        String supplierCode,
        String supplierContract,
        String currency,
        String createdBy,
        String referenceNo,
        String remark
) {}
