package com.consignment.service.model.setup;

import java.util.List;

public record InternalSupplierSetupResponse(
        String setupId,
        String supplierCode,
        String supplierStore,
        List<ConsigneeGroup> consignees
) {
    public record ConsigneeGroup(
            String companyId,
            List<String> stores
    ) {}
}
