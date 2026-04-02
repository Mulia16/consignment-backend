package com.consignment.service.model.setup;

import java.time.Instant;

public record ExternalSupplierSetupResponse(
        String id,
        String supplierCode,
        String supplierType,
        String contractNumber,
        String consigneeCompany,
        String consigneeStore,
        int currentInventoryQty,
        Instant createdAt,
        Instant updatedAt
) {
}
