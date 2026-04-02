package com.consignment.service.model.csrq;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CsrqRequest(
        @NotBlank String company,
        @NotBlank String store,
        @NotBlank String supplierCode,
        @NotBlank String supplierContract,
        String branch,
        String internalSupplierStore,
        String notes,
        @NotBlank String createdBy,
        @NotBlank String createdMethod,
        String referenceNo,
        @Valid @NotEmpty List<CsrqDetailRequest> items
) {
}
