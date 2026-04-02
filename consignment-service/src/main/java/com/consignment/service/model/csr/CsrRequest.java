package com.consignment.service.model.csr;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CsrRequest(
        @NotBlank String company,
        @NotBlank String store,
        @NotBlank String supplierCode,
        @NotBlank String supplierContract,
        String internalSupplierStore,
        String supplierConfirmNote,
        String reasonCode,
        String remark,
        @NotBlank String createdBy,
        String referenceNo,
        @Valid @NotEmpty List<CsrDetailRequest> items
) {
}
