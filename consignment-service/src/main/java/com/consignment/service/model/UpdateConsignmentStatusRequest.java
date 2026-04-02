package com.consignment.service.model;

import com.consignment.service.domain.ConsignmentStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateConsignmentStatusRequest(
        @NotNull ConsignmentStatus status,
        String message
) {
}
