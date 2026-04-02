package com.consignment.service.model.csrv;

public record CsrvSearchCriteria(
        String company,
        String receivingStore,
        String supplierCode,
        String supplierContract,
        String branch,
        String createdMethod,
        String referenceNo,
        String itemCode,
        String status
) {
}
