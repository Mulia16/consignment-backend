package com.consignment.service.persistence.model;

import java.time.Instant;
import java.time.LocalDate;

public class CsrvHeaderEntity {

    private String id;
    private String docNo;
    private String company;
    private String receivingStore;
    private String supplierCode;
    private String supplierContract;
    private String branch;
    private String supplierDoNo;
    private LocalDate deliveryDate;
    private String remark;
    private String status;
    private String createdBy;
    private String createdMethod;
    private String referenceNo;
    private Instant releasedAt;
    private Instant createdAt;
    private Instant updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDocNo() { return docNo; }
    public void setDocNo(String docNo) { this.docNo = docNo; }
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
    public String getReceivingStore() { return receivingStore; }
    public void setReceivingStore(String receivingStore) { this.receivingStore = receivingStore; }
    public String getSupplierCode() { return supplierCode; }
    public void setSupplierCode(String supplierCode) { this.supplierCode = supplierCode; }
    public String getSupplierContract() { return supplierContract; }
    public void setSupplierContract(String supplierContract) { this.supplierContract = supplierContract; }
    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }
    public String getSupplierDoNo() { return supplierDoNo; }
    public void setSupplierDoNo(String supplierDoNo) { this.supplierDoNo = supplierDoNo; }
    public LocalDate getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(LocalDate deliveryDate) { this.deliveryDate = deliveryDate; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
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
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
