package com.consignment.service.model.billing;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CustomerBillingComputeRequest(
        @NotBlank String company,
        @NotBlank String store,
        @NotNull LocalDate fromDate,
        @NotNull LocalDate toDate,
        @NotBlank String periodType,   // MONTHLY / WEEKLY
        String customerCode,           // null = all customers under this store
        @NotBlank String createdBy
) {}
