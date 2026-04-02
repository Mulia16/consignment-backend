package com.consignment.service.model.csrv;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.time.LocalDate;
import java.util.List;

public record CsrvRequest(
        @NotBlank String company,
        @NotBlank String receivingStore,
        @NotBlank String supplierCode,
        @NotBlank String supplierContract,
        String branch,
        String supplierDoNo,
        LocalDate deliveryDate,
        String remark,
        @NotBlank String createdBy,
        @NotBlank String createdMethod,
        String referenceNo,
        @Valid @NotEmpty List<CsrvDetailRequest> items
) {
}
