package com.consignment.service.model.csrq;

import java.math.BigDecimal;

public record CsrqResponseDetail(
        String id,
        String itemCode,
        BigDecimal requestQty,
        String requestUom
) {
}
