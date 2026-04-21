package com.consignment.service.model.consignee;

public record ConsigneeProductResponse(
        String itemCode,
        String itemName,
        String variant,
        String supplierCode,
        String supplierContract,
        String consigneeStore
) {
}
