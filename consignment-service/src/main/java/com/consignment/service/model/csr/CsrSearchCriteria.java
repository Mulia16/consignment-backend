package com.consignment.service.model.csr;

public record CsrSearchCriteria(
        String company,
        String store,
        String supplierCode,
        String supplierContract,
        String status,
        String createdBy,
        String referenceNo,
        String itemCode
) {}
