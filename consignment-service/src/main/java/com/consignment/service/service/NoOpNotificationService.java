package com.consignment.service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NoOpNotificationService implements NotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NoOpNotificationService.class);

    @Override
    public void sendCsrqReleased(String docNo, String supplierCode) {
        LOGGER.info("CSRQ release notification queued. docNo={}, supplierCode={}", docNo, supplierCode);
    }

    @Override
    public void sendCsrReleased(String docNo, String supplierCode) {
        LOGGER.info("CSR release notification queued. docNo={}, supplierCode={}", docNo, supplierCode);
    }
}
