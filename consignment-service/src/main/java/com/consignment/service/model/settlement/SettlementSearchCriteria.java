package com.consignment.service.model.settlement;

public record SettlementSearchCriteria(
        String docNo,
        String company,
        String store,
        String settlementType,
        String customerCode,
        String supplierCode,
        String supplierContract,
        String status,
        String createdBy,
        String referenceNo,
        int page,
        int perPage
) {
    public int offset() { return (page - 1) * perPage; }
}
