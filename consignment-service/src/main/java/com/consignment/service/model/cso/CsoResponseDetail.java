package com.consignment.service.model.cso;

import java.math.BigDecimal;

public record CsoResponseDetail(
        String id,
        String itemCode,
        BigDecimal qty,
        String uom
) {
}
