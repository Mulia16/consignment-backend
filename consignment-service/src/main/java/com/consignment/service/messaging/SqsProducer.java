package com.consignment.service.messaging;

import com.consignment.service.config.QueueProperties;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * SQS producer for outbound events.
 * Enabled only when app.queue.enabled=true.
 */
@Component
@ConditionalOnProperty(name = "app.queue.enabled", havingValue = "true")
public class SqsProducer {

    private static final Logger log = LoggerFactory.getLogger(SqsProducer.class);

    private final SqsTemplate sqsTemplate;
    private final QueueProperties queueProperties;

    public SqsProducer(SqsTemplate sqsTemplate, QueueProperties queueProperties) {
        this.sqsTemplate = sqsTemplate;
        this.queueProperties = queueProperties;
    }

    public void publishSettlementDoc(String docType, String docId, Map<String, Object> data) {
        publish(queueProperties.settlementDocs(), docType + "_POSTED", data);
    }

    public void publishAvailableStock(String store, String sku, Map<String, Object> data) {
        publish(queueProperties.availableStock(), "STOCK_UPDATED", data);
    }

    public void publishMasterSyncResult(String entityType, Map<String, Object> data) {
        publish(queueProperties.masterSync(), "MASTER_SYNC_" + entityType.toUpperCase(), data);
    }

    private void publish(String queueName, String eventType, Map<String, Object> data) {
        String correlationId = UUID.randomUUID().toString();
        SqsMessagePayload payload = new SqsMessagePayload(eventType, correlationId, data);
        try {
            sqsTemplate.send(queueName, payload);
            log.info("[SQS] Published event={} to queue={} correlationId={}", eventType, queueName, correlationId);
        } catch (Exception e) {
            log.error("[SQS] Failed to publish event={} to queue={}: {}", eventType, queueName, e.getMessage(), e);
            throw new RuntimeException("SQS publish failed for event: " + eventType, e);
        }
    }
}
