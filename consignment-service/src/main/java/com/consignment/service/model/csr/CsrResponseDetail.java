package com.consignment.service.model.csr;

import java.math.BigDecimal;

public record CsrResponseDetail(
        String id,
        String itemCode,
        String uom,
        BigDecimal qty,
        BigDecimal actualQty
) {
}
