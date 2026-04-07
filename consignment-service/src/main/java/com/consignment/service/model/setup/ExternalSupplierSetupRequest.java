package com.consignment.service.model.setup;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ExternalSupplierSetupRequest(
        @NotBlank String supplierCode,
        @NotBlank String supplierType,
        @NotBlank String contractNumber,
        @NotEmpty @Valid List<ConsigneeRequest> consignees
) {
    public record ConsigneeRequest(
            @NotBlank String companyId,
            @NotEmpty List<String> storeIds
    ) {}
}
