package com.consignment.service.model.csrq;

public record CsrqSearchCriteria(
        String company,
        String store,
        String supplierCode,
        String supplierContract,
        String branch,
        String internalSupplierStore,
        String createdMethod,
        String referenceNo,
        String itemCode,
        String status
) {
}
