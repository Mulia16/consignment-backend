package com.consignment.service.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class ApiLoggingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiLoggingService.class);

    private final ApiLogRepository apiLogRepository;
    private final boolean enabled;

    public ApiLoggingService(
            ApiLogRepository apiLogRepository,
            @Value("${app.api-log.enabled:false}") boolean enabled
    ) {
        this.apiLogRepository = apiLogRepository;
        this.enabled = enabled;
    }

    public void logInbound(
            String endpoint,
            String correlationId,
            String requestPayload,
            String responsePayload,
            int statusCode,
            long processingTimeMs
    ) {
        if (!enabled) {
            return;
        }

        ApiLogDocument document = new ApiLogDocument();
        document.setTimestamp(Instant.now());
        document.setDirection("INBOUND");
        document.setEndpoint(endpoint);
        document.setCorrelationId(correlationId);
        document.setRequestPayload(trimPayload(requestPayload));
        document.setResponsePayload(trimPayload(responsePayload));
        document.setStatusCode(statusCode);
        document.setProcessingTimeMs(processingTimeMs);

        try {
            apiLogRepository.save(document);
        } catch (Exception exception) {
            LOGGER.warn("Failed to persist API log for endpoint={}", endpoint, exception);
        }
    }

    private String trimPayload(String payload) {
        if (payload == null) {
            return null;
        }
        return payload.length() > 4000 ? payload.substring(0, 4000) : payload;
    }
}
