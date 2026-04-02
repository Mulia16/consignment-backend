package com.consignment.service.model.csr;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CsrActualQtyUpdateRequest(
        @NotNull @DecimalMin(value = "0.0") BigDecimal actualQty
) {
}
