package com.consignment.service.model.consignee;

import java.time.LocalDate;

public record ConsigneePurchaseOrderResponse(
        String poNumber,
        String store,
        String itemCode,
        String itemName,
        Integer orderedQty,
        String status,
        LocalDate poDate
) {
}
