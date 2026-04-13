package com.consignment.service.service;

public interface NotificationService {

    void sendCsrqReleased(String docNo, String supplierCode);

    void sendCsrnReleased(String docNo, String supplierCode);
}
