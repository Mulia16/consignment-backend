package com.consignment.service.persistence.model;

import java.time.Instant;

public class CsrHeaderEntity {

    private String id;
    private String docNo;
    private String company;
    private String store;
    private String supplierCode;
    private String supplierContract;
    private String internalSupplierStore;
    private String supplierConfirmNote;
    private String reasonCode;
    private String remark;
    private String status;
    private String createdBy;
    private String referenceNo;
    private Instant releasedAt;
    private Instant completedAt;
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
    public String getSupplierCode() { return supplierCode; }
    public void setSupplierCode(String supplierCode) { this.supplierCode = supplierCode; }
    public String getSupplierContract() { return supplierContract; }
    public void setSupplierContract(String supplierContract) { this.supplierContract = supplierContract; }
    public String getInternalSupplierStore() { return internalSupplierStore; }
    public void setInternalSupplierStore(String internalSupplierStore) { this.internalSupplierStore = internalSupplierStore; }
    public String getSupplierConfirmNote() { return supplierConfirmNote; }
    public void setSupplierConfirmNote(String supplierConfirmNote) { this.supplierConfirmNote = supplierConfirmNote; }
    public String getReasonCode() { return reasonCode; }
    public void setReasonCode(String reasonCode) { this.reasonCode = reasonCode; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public String getReferenceNo() { return referenceNo; }
    public void setReferenceNo(String referenceNo) { this.referenceNo = referenceNo; }
    public Instant getReleasedAt() { return releasedAt; }
    public void setReleasedAt(Instant releasedAt) { this.releasedAt = releasedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
