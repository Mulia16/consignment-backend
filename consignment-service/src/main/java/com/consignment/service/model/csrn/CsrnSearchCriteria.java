package com.consignment.service.model.csrn;

public record CsrnSearchCriteria(
        String docNo,
        String company,
        String store,
        String supplierCode,
        String supplierContract,
        String internalSupplierStore,
        String status,
        String createdBy,
        String referenceNo,
        String reasonCode,
        String itemCode,
        int page,
        int perPage
) {
    public int offset() { return (page - 1) * perPage; }
}
