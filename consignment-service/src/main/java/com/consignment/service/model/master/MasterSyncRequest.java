package com.consignment.service.model.master;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record MasterSyncRequest(
        @Valid @NotEmpty List<MasterSyncRecordRequest> records
) {
}
