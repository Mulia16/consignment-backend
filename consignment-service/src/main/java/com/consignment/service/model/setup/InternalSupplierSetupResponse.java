package com.consignment.service.model.setup;

import java.time.Instant;

public record InternalSupplierSetupResponse(
        String id,
        String supplierCode,
        String supplierStore,
        String consigneeCompany,
        String consigneeStore,
        Instant createdAt
) {
}
