package com.consignment.service.model.csrv;

import com.consignment.service.config.EmptyStringToNullLocalDateDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.time.LocalDate;
import java.util.List;

public record CsrvUpdateRequest(
        String branch,
        String supplierDoNo,
        @JsonDeserialize(using = EmptyStringToNullLocalDateDeserializer.class) LocalDate deliveryDate,
        String remark,
        String referenceNo,
        @Valid @NotEmpty List<CsrvDetailRequest> items
) {
}
