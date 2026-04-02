package com.consignment.service.model.setup;

import jakarta.validation.constraints.NotBlank;

public record InternalSupplierSetupRequest(
        @NotBlank String supplierCode,
        @NotBlank String supplierStore,
        @NotBlank String consigneeCompany,
        @NotBlank String consigneeStore
) {
}
