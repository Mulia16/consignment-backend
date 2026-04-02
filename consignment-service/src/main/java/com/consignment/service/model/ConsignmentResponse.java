package com.consignment.service.model;

import java.time.Instant;

public record ConsignmentResponse(
        String requestId,
        String sku,
        int quantity,
        String requestStore,
        String supplier,
        String status,
        String message,
        Instant createdAt,
        Instant updatedAt
) {
}
