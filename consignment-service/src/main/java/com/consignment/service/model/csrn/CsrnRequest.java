package com.consignment.service.model.csrn;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CsrnRequest(
        @NotBlank String company,
        @NotBlank String store,
        @NotBlank String supplierCode,
        @NotBlank String supplierContract,
        @NotBlank String csoDocNo,
        String internalSupplierStore,
        String reasonCode,
        String remark,
        @NotBlank String createdBy,
        String referenceNo,
        @Valid @NotEmpty List<CsrnDetailRequest> items
) {
}
