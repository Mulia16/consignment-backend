package com.consignment.service.model.settlement;

import java.math.BigDecimal;
import java.time.Instant;

public record SettlementDetailResponse(
        String id,
        String documentType,
        String documentNo,
        String itemCode,
        BigDecimal qty,
        String uom,
        BigDecimal unitPrice,
        BigDecimal lineAmount,
        String remark,
        Instant createdAt
) {}
