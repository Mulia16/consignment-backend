package com.consignment.service.model.csrv;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CsrvDetailRequest(
        @NotBlank String itemCode,
        @NotNull @DecimalMin(value = "0.0") BigDecimal availableQty,
        @NotNull @DecimalMin(value = "0.0001") BigDecimal requestQty,
        @NotNull @DecimalMin(value = "0.0001") BigDecimal receivingQty
) {
}
