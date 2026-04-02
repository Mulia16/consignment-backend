package com.consignment.service.model.csdo;

import jakarta.validation.constraints.NotBlank;

public record CsdoTransferRequest(
        boolean requireGenerateCdo,
        String shippingMode,
        String transporter,
        @NotBlank String createdBy
) {
}
