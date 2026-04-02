package com.consignment.inventory.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ReserveRequest(
        @NotBlank String sku,
        @Min(1) int quantity
) {
}
