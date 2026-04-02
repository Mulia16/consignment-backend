package com.consignment.service.model.cso;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CsoRequest(
        @NotBlank String company,
        @NotBlank String store,
        @NotBlank String customerCode,
        String customerBranch,
        String customerEmail,
        @NotBlank String supplierCode,
        @NotBlank String supplierContract,
        boolean autoGenerateCsdo,
        String note,
        @NotBlank String createdBy,
        @NotBlank String createdMethod,
        String referenceNo,
        @Valid @NotEmpty List<CsoDetailRequest> items
) {
}
