package com.consignment.service.model.setup;

import java.util.List;

public record ConsignmentSetupItemResponse(
        String itemCode,
        List<ExternalSupplierSetupResponse> externalSuppliers,
        List<InternalSupplierSetupResponse> internalSuppliers
) {
}
