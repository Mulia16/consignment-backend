package com.consignment.service.model.csrv;

import java.math.BigDecimal;

public record CsrvResponseDetail(
        String id,
        String itemCode,
        BigDecimal availableQty,
        BigDecimal requestQty,
        BigDecimal receivingQty
) {
}
