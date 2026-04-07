package com.consignment.service.model.csrv;

public record CsrvSearchCriteria(
        String docNo,
        String company,
        String receivingStore,
        String supplierCode,
        String supplierContract,
        String branch,
        String supplierDoNo,
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
