package com.consignment.service.model.settlement;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record SettlementDocumentPostRequest(
        @Valid @NotEmpty List<SettlementDocumentSourceRequest> documents
) {
}