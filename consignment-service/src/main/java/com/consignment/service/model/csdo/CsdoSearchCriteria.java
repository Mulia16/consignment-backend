package com.consignment.service.model.csdo;

public record CsdoSearchCriteria(
        String docNo,
        String company,
        String store,
        String customerCode,
        String csoDocNo,
        String status,
        String createdBy,
        String itemCode,
        int page,
        int perPage
) {
    public int offset() { return (page - 1) * perPage; }
}
