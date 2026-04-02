package com.consignment.service.persistence.model;

import java.time.Instant;

public class CsoHeaderEntity {

    private String id;
    private String docNo;
    private String company;
    private String store;
    private String customerCode;
    private String customerBranch;
    private String customerEmail;
    private String supplierCode;
    private String supplierContract;
    private boolean autoGenerateCsdo;
    private String note;
    private String status;
    private String createdBy;
    private String createdMethod;
    private String referenceNo;
    private Instant releasedAt;
    private String releasedBy;
    private Instant createdAt;
    private Instant updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDocNo() { return docNo; }
    public void setDocNo(String docNo) { this.docNo = docNo; }
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
    public String getStore() { return store; }
    public void setStore(String store) { this.store = store; }
    public String getCustomerCode() { return customerCode; }
    public void setCustomerCode(String customerCode) { this.customerCode = customerCode; }
    public String getCustomerBranch() { return customerBranch; }
    public void setCustomerBranch(String customerBranch) { this.customerBranch = customerBranch; }
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    public String getSupplierCode() { return supplierCode; }
    public void setSupplierCode(String supplierCode) { this.supplierCode = supplierCode; }
    public String getSupplierContract() { return supplierContract; }
    public void setSupplierContract(String supplierContract) { this.supplierContract = supplierContract; }
    public boolean isAutoGenerateCsdo() { return autoGenerateCsdo; }
    public void setAutoGenerateCsdo(boolean autoGenerateCsdo) { this.autoGenerateCsdo = autoGenerateCsdo; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public String getCreatedMethod() { return createdMethod; }
    public void setCreatedMethod(String createdMethod) { this.createdMethod = createdMethod; }
    public String getReferenceNo() { return referenceNo; }
    public void setReferenceNo(String referenceNo) { this.referenceNo = referenceNo; }
    public Instant getReleasedAt() { return releasedAt; }
    public void setReleasedAt(Instant releasedAt) { this.releasedAt = releasedAt; }
    public String getReleasedBy() { return releasedBy; }
    public void setReleasedBy(String releasedBy) { this.releasedBy = releasedBy; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
