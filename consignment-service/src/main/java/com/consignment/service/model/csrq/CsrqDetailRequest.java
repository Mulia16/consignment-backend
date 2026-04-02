package com.consignment.service.model.csrq;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CsrqDetailRequest(
        @NotBlank String itemCode,
        @NotNull @DecimalMin(value = "0.0001") BigDecimal requestQty,
        @NotBlank String requestUom
) {
}
