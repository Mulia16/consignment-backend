package com.consignment.service.model.csa;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CsaDetailRequest(
        @NotBlank String itemCode,
        String itemName,
        @NotNull @DecimalMin(value = "0.0001") BigDecimal qty,
        @NotBlank String uom,
        @NotBlank String settlementDecision
) {
}
