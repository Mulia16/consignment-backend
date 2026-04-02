package com.consignment.service.model.setup;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ExternalSupplierSetupRequest(
        @NotBlank String supplierCode,
        @NotBlank String supplierType,
        @NotBlank String contractNumber,
        @NotBlank String consigneeCompany,
        @NotBlank String consigneeStore,
        @Min(0) int currentInventoryQty
) {
}
