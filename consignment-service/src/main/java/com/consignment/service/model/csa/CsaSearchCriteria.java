package com.consignment.service.model.csa;

public record CsaSearchCriteria(
        String company,
        String store,
        String transactionType,
        String status,
        String createdBy,
        String referenceNo
) {}
