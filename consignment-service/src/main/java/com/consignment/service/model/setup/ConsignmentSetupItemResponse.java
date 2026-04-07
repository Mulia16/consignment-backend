package com.consignment.service.model.setup;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.util.List;

public record ConsignmentSetupItemResponse(
        String itemCode,
        String itemName,
        String variant,
        String hierarchy,
        String itemModel,
        BigDecimal unitRetail,
        BigDecimal mvc,
        CategoryHierarchy category,
        List<ExternalSupplierSetupResponse> externalSuppliers,
        List<InternalSupplierSetupResponse> internalSuppliers
) {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record CategoryHierarchy(
            String l1,
            String l2,
            String l3
    ) {}
}
