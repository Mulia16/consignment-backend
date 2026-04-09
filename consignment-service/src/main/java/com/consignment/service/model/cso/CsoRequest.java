package com.consignment.service.model.cso;

import com.consignment.service.config.EmptyStringToNullLocalDateDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record CsoRequest(
        @NotBlank @Size(max = 50) String company,
        @NotBlank @Size(max = 50) String store,
        @NotBlank @Size(max = 50) String customerCode,
        @Size(max = 50) String customerBranch,
        @Email @Size(max = 150) String customerEmail,
        @NotBlank @Size(max = 50) String supplierCode,
        @NotBlank @Size(max = 100) String supplierContract,
        boolean autoGenerateCsdo,
        String note,
        @NotBlank @Size(max = 50) String createdBy,
        @NotBlank @Size(max = 20) String createdMethod,
        @Size(max = 100) String referenceNo,
        @Size(max = 100) String shippingTerm,
        @JsonDeserialize(using = EmptyStringToNullLocalDateDeserializer.class) LocalDate deliveryDate,
        @Size(max = 50) String shippingMode,
        @Size(max = 100) String transporter,
        @Size(max = 200) String shippingTo,
        String shippingAddress,
        @Size(max = 100) String customerReference,
        String transportInformation,
        @Valid @NotEmpty List<CsoDetailRequest> items
) {
}
