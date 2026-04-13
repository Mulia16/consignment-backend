package com.consignment.service.model.csrn;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CsrnDetailRequest(
        @NotBlank String itemCode,
        @NotBlank String uom,
        @NotNull @DecimalMin(value = "0.0001") BigDecimal qty,
        BigDecimal actualQty
) {
}
