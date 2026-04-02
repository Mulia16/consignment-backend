package com.consignment.service.model.cso;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CsoDetailRequest(
        @NotBlank String itemCode,
        @NotNull @DecimalMin(value = "0.0001") BigDecimal qty,
        @NotBlank String uom
) {
}
