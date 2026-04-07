package com.consignment.service.model.csrq;

public record CsrqSearchCriteria(
        String docNo,
        String company,
        String store,
        String supplierCode,
        String supplierContract,
        String branch,
        String internalSupplierStore,
        String createdMethod,
        String referenceNo,
        String itemCode,
        String status,
        String createdBy,
        int page,
        int perPage
) {
    public int offset() { return (page - 1) * perPage; }
}
