package com.consignment.service.model.cso;

import com.consignment.service.config.EmptyStringToNullLocalDateDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.time.LocalDate;
import java.util.List;

public record CsoRequest(
        @NotBlank String company,
        @NotBlank String store,
        @NotBlank String customerCode,
        String customerBranch,
        String customerEmail,
        @NotBlank String supplierCode,
        @NotBlank String supplierContract,
        boolean autoGenerateCsdo,
        String note,
        @NotBlank String createdBy,
        @NotBlank String createdMethod,
        String referenceNo,
        String shippingTerm,
        @JsonDeserialize(using = EmptyStringToNullLocalDateDeserializer.class) LocalDate deliveryDate,
        String shippingMode,
        String transporter,
        String shippingTo,
        String shippingAddress,
        String customerReference,
        String transportInformation,
        @Valid @NotEmpty List<CsoDetailRequest> items
) {
}
