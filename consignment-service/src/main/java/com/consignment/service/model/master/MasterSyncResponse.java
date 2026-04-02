package com.consignment.service.model.master;

import java.time.Instant;

public record MasterSyncResponse(
        String entity,
        int upserted,
        Instant syncedAt
) {
}
