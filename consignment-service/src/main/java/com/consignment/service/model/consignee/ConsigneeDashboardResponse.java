package com.consignment.service.model.consignee;

public record ConsigneeDashboardResponse(
        String itemCode,
        String itemName,
        Integer salesQty,
        Integer currentStockQty,
        String store
) {
}
