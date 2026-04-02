package com.consignment.service.model.settlement;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record SettlementDocumentSourceRequest(
        @NotBlank String documentType,
        @NotBlank String documentId,
        @DecimalMin(value = "0.0") BigDecimal unitPrice,
        String remark
) {
}