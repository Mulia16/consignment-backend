package com.consignment.service.model.billing;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record UnpostSalesSyncRequest(
        @NotEmpty List<UnpostSalesLine> lines
) {
    public record UnpostSalesLine(
            @NotBlank String store,
            @NotBlank String sku,
            @NotNull BigDecimal salesQty,
            @NotNull BigDecimal salesReturnQty,
            @NotNull LocalDate salesDate,
            @NotBlank String sourceType,   // POS / B2B / ONLINE
            String sourceRef
    ) {}
}
