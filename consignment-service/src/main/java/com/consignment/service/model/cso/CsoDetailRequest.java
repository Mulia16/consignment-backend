package com.consignment.service.model.cso;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CsoDetailRequest(
        @NotBlank @Size(max = 50) String itemCode,
        @NotNull @DecimalMin(value = "0.0001") BigDecimal qty,
        @NotBlank @Size(max = 20) String uom
) {
}
