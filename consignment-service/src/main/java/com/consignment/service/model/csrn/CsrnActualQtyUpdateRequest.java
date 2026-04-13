package com.consignment.service.model.csrn;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CsrnActualQtyUpdateRequest(
        @NotNull @DecimalMin(value = "0.0") BigDecimal actualQty
) {
}
