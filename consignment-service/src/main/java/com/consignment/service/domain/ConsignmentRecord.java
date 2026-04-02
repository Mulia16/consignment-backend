package com.consignment.service.domain;

import java.time.Instant;

public class ConsignmentRecord {

    private final String requestId;
    private final String sku;
    private final int quantity;
    private final String requestStore;
    private final String supplier;
    private ConsignmentStatus status;
    private String message;
    private final Instant createdAt;
    private Instant updatedAt;

    public ConsignmentRecord(
            String requestId,
            String sku,
            int quantity,
            String requestStore,
            String supplier,
            ConsignmentStatus status,
            String message,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.requestId = requestId;
        this.sku = sku;
        this.quantity = quantity;
        this.requestStore = requestStore;
        this.supplier = supplier;
        this.status = status;
        this.message = message;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getSku() {
        return sku;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getRequestStore() {
        return requestStore;
    }

    public String getSupplier() {
        return supplier;
    }

    public ConsignmentStatus getStatus() {
        return status;
    }

    public void setStatus(ConsignmentStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
