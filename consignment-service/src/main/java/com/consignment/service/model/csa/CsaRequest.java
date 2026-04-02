package com.consignment.service.model.csa;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CsaRequest(
        @NotBlank String company,
        @NotBlank String store,
        String supplierCode,
        String supplierContract,
        @NotBlank String transactionType,
        String referenceNo,
        String reasonCode,
        String remark,
        String createFrom,
        @NotBlank String createdBy,
        @Valid @NotEmpty List<CsaDetailRequest> items
) {
}
