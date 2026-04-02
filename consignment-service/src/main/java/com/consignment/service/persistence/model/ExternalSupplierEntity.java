package com.consignment.service.persistence.model;

import java.time.Instant;

public class ExternalSupplierEntity {

    private String id;
    private String itemCode;
    private String supplierCode;
    private String supplierType;
    private String supplierContract;
    private String consigneeCompany;
    private String consigneeStore;
    private int currentInventoryQty;
    private Instant createdAt;
    private Instant updatedAt;

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

    public String getSupplierType() {
        return supplierType;
    }

    public void setSupplierType(String supplierType) {
        this.supplierType = supplierType;
    }

    public String getSupplierContract() {
        return supplierContract;
    }

    public void setSupplierContract(String supplierContract) {
        this.supplierContract = supplierContract;
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

    public int getCurrentInventoryQty() {
        return currentInventoryQty;
    }

    public void setCurrentInventoryQty(int currentInventoryQty) {
        this.currentInventoryQty = currentInventoryQty;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
