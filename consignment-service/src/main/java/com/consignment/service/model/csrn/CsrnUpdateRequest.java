package com.consignment.service.model.csrn;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CsrnUpdateRequest(
        String reasonCode,
        String remark,
        @NotBlank String updatedBy,
        @Valid @NotEmpty List<CsrnDetailRequest> items
) {
}
