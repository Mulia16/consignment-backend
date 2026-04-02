package com.consignment.service.model.csa;

import java.math.BigDecimal;

public record CsaResponseDetail(
        String id,
        String itemCode,
        String itemName,
        BigDecimal qty,
        String uom,
        String settlementDecision
) {
}
