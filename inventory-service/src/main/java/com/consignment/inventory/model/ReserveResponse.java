package com.consignment.inventory.model;

public record ReserveResponse(String sku, int requestedQty, boolean reserved, int remainingQty) {
}
