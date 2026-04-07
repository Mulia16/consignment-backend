package com.consignment.service.model.cso;

public record CsoSearchCriteria(
        String docNo,
        String company,
        String store,
        String customerCode,
        String supplierCode,
        String supplierContract,
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
