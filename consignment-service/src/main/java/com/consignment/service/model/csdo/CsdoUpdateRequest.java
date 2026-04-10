package com.consignment.service.model.csdo;

public record CsdoUpdateRequest(
        boolean requireGenerateCdo,
        String shippingMode,
        String transporter
) {
}
