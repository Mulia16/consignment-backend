package com.consignment.service.model.csrn;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CsrnUpdateRequest(
        String internalSupplierStore,
        String supplierConfirmNote,
        String reasonCode,
        String remark,
        String referenceNo,
        String csoDocNo,
        @Valid @NotEmpty List<CsrnDetailRequest> items
) {
}
