package com.consignment.service.model.settlement;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SettlementBatchGenerateRequest(
        @NotBlank String company,
        @NotBlank String store,
        @NotBlank String settlementType,
        String customerCode,
        String supplierCode,
        String supplierContract,
        String currency,
        @NotBlank String createdBy,
        LocalDate fromDate,
        LocalDate toDate,
        @DecimalMin(value = "0.0") BigDecimal defaultUnitPrice,
        String referenceNo
) {
}