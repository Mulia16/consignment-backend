package com.consignment.service.persistence.model;

import java.time.Instant;

public class InternalSupplierEntity {

    private String id;
    private String itemCode;
    private String supplierCode;
    private String supplierStore;
    private String consigneeCompany;
    private String consigneeStore;
    private Instant createdAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public String getSupplierCode() {
        return supplierCode;
    }

    public void setSupplierCode(String supplierCode) {
        this.supplierCode = supplierCode;
    }

    public String getSupplierStore() {
        return supplierStore;
    }

    public void setSupplierStore(String supplierStore) {
        this.supplierStore = supplierStore;
    }

    public String getConsigneeCompany() {
        return consigneeCompany;
    }

    public void setConsigneeCompany(String consigneeCompany) {
        this.consigneeCompany = consigneeCompany;
    }

    public String getConsigneeStore() {
        return consigneeStore;
    }

    public void setConsigneeStore(String consigneeStore) {
        this.consigneeStore = consigneeStore;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
