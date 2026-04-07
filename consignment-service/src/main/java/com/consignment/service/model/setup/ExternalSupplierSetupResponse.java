package com.consignment.service.model.setup;

import java.util.List;

public record ExternalSupplierSetupResponse(
        String setupId,
        String supplierCode,
        String supplierType,
        String contractNumber,
        List<ConsigneeGroup> consignees
) {
    public record ConsigneeGroup(
            String companyId,
            List<String> stores
    ) {}
}
