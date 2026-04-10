package com.consignment.service.model.csr;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CsrUpdateRequest(
        String internalSupplierStore,
        String supplierConfirmNote,
        String reasonCode,
        String remark,
        String referenceNo,
        String csoDocNo,
        @Valid @NotEmpty List<CsrDetailRequest> items
) {
}
