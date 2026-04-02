package com.consignment.service.service;

public interface NotificationService {

    void sendCsrqReleased(String docNo, String supplierCode);

    void sendCsrReleased(String docNo, String supplierCode);
}
