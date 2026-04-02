package com.consignment.service.messaging;

import java.util.Map;

/**
 * Generic SQS message envelope.
 * eventType maps to a handler (e.g. MASTER_SYNC_ITEM, CSO_AUTO_CREATE, etc.)
 */
public record SqsMessagePayload(
        String eventType,
        String correlationId,
        Map<String, Object> data
) {}
