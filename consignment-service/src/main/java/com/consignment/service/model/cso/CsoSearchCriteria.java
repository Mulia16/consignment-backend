package com.consignment.service.model.cso;

public record CsoSearchCriteria(
        String company,
        String store,
        String customerCode,
        String supplierCode,
        String supplierContract,
        String createdMethod,
        String referenceNo,
        String itemCode,
        String status
) {
}
