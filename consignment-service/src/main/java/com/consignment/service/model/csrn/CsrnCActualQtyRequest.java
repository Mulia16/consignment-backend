package com.consignment.service.model.csrn;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CsrnCActualQtyRequest(
        @NotNull BigDecimal actualQty
) {
}
