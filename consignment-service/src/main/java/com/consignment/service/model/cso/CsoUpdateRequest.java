package com.consignment.service.model.cso;

import com.consignment.service.config.EmptyStringToNullLocalDateDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.time.LocalDate;
import java.util.List;

public record CsoUpdateRequest(
        String customerBranch,
        String customerEmail,
        String note,
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
