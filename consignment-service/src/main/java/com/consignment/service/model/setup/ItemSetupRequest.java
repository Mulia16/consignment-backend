package com.consignment.service.model.setup;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record ItemSetupRequest(
        @NotBlank String itemCode,
        String itemName,
        String variant,
        @NotBlank String hierarchy,
        String itemModel,
        BigDecimal unitRetail,
        BigDecimal mvc,
        String categoryL1,
        String categoryL2,
        String categoryL3
) {}
