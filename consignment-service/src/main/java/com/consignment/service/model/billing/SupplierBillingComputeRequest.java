package com.consignment.service.model.billing;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record SupplierBillingComputeRequest(
        @NotBlank String company,
        String store,                       // null = all stores under company
        @NotBlank String supplierCode,
        @NotNull LocalDate fromDate,
        @NotNull LocalDate toDate,
        @NotBlank String periodType,        // MONTHLY / WEEKLY
        boolean carryForwardDecimal,
        @NotBlank String createdBy
) {}
