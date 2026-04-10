package com.consignment.service.model.csrq;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CsrqUpdateRequest(
        String branch,
        String internalSupplierStore,
        String notes,
        String referenceNo,
        @Valid @NotEmpty List<CsrqDetailRequest> items
) {
}
