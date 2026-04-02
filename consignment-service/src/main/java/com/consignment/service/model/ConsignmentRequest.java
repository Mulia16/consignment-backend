package com.consignment.service.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ConsignmentRequest(
        @NotBlank String sku,
        @Min(1) int quantity,
        @NotBlank String requestStore,
        @NotBlank String supplier
) {
}
