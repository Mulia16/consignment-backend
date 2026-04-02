package com.consignment.service.model.csdo;

public record CsdoSearchCriteria(
        String company,
        String store,
        String customerCode,
        String status,
        String createdMethod,
        String referenceNo,
        String itemCode
) {}
