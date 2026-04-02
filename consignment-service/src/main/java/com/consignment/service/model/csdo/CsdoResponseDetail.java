package com.consignment.service.model.csdo;

import java.math.BigDecimal;

public record CsdoResponseDetail(
        String id,
        String itemCode,
        BigDecimal qty,
        String uom
) {
}
