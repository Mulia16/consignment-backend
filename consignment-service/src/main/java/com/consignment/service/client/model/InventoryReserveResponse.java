package com.consignment.service.client.model;

public record InventoryReserveResponse(String sku, int requestedQty, boolean reserved, int remainingQty) {
}
