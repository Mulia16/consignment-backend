package com.consignment.service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.queue")
public record QueueProperties(
        boolean enabled,
        String masterSync,
        String posSales,
        String csrvAuto,
        String csoAuto,
        String adjAuto,
        String availableStock,
        String settlementDocs
) {
}
