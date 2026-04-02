package com.consignment.service.messaging;

import com.consignment.service.model.cso.CsoRequest;
import com.consignment.service.model.csrv.CsrvRequest;
import com.consignment.service.service.CsoService;
import com.consignment.service.service.CsrvService;
import com.consignment.service.service.MasterDataSyncService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * SQS consumers for inbound events.
 * Enabled only when app.queue.enabled=true to allow local dev without AWS.
 */
@Component
@ConditionalOnProperty(name = "app.queue.enabled", havingValue = "true")
public class SqsConsumer {

    private static final Logger log = LoggerFactory.getLogger(SqsConsumer.class);

    private final CsoService csoService;
    private final CsrvService csrvService;
    private final MasterDataSyncService masterDataSyncService;
    private final ObjectMapper objectMapper;

    public SqsConsumer(CsoService csoService, CsrvService csrvService,
                       MasterDataSyncService masterDataSyncService, ObjectMapper objectMapper) {
        this.csoService = csoService;
        this.csrvService = csrvService;
        this.masterDataSyncService = masterDataSyncService;
        this.objectMapper = objectMapper;
    }

    /** Consumes CSO auto-create events from ACMM */
    @SqsListener("${app.queue.cso-auto}")
    public void onCsoAutoCreate(SqsMessagePayload payload) {
        log.info("[SQS] CSO_AUTO_CREATE received, correlationId={}", payload.correlationId());
        try {
            CsoRequest request = objectMapper.convertValue(payload.data(), CsoRequest.class);
            csoService.autoCreate(request);
            log.info("[SQS] CSO auto-created successfully, correlationId={}", payload.correlationId());
        } catch (Exception e) {
            log.error("[SQS] CSO_AUTO_CREATE failed, correlationId={}: {}", payload.correlationId(), e.getMessage(), e);
            throw e; // rethrow for DLQ
        }
    }

    /** Consumes CSRV auto-create events from ACMM */
    @SqsListener("${app.queue.csrv-auto}")
    public void onCsrvAutoCreate(SqsMessagePayload payload) {
        log.info("[SQS] CSRV_AUTO_CREATE received, correlationId={}", payload.correlationId());
        try {
            CsrvRequest request = objectMapper.convertValue(payload.data(), CsrvRequest.class);
            csrvService.autoCreate(request);
            log.info("[SQS] CSRV auto-created successfully, correlationId={}", payload.correlationId());
        } catch (Exception e) {
            log.error("[SQS] CSRV_AUTO_CREATE failed, correlationId={}: {}", payload.correlationId(), e.getMessage(), e);
            throw e;
        }
    }
}
