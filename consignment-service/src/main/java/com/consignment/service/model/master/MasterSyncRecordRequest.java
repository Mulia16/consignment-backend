package com.consignment.service.model.master;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record MasterSyncRecordRequest(
        @NotBlank String code,
        Map<String, Object> attributes
) {
}
