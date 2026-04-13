package com.consignment.service.model.csrn;

import java.math.BigDecimal;

public record CsrnResponseDetail(
        String id,
        String itemCode,
        String uom,
        BigDecimal qty,
        BigDecimal actualQty
) {
}
