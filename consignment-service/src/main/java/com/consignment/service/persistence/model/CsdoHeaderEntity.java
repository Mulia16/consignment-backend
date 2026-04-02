package com.consignment.service.persistence.model;

import java.time.Instant;

public class CsdoHeaderEntity {

    private String id;
    private String docNo;
    private String csoId;
    private String csoDocNo;
    private String company;
    private String store;
    private String customerCode;
    private String customerBranch;
    private String customerEmail;
    private boolean requireGenerateCdo;
    private String shippingMode;
    private String transporter;
    private String status;
    private String createdBy;
    private Instant releasedAt;
    private Instant createdAt;
    private Instant updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDocNo() { return docNo; }
    public void setDocNo(String docNo) { this.docNo = docNo; }
    public String getCsoId() { return csoId; }
    public void setCsoId(String csoId) { this.csoId = csoId; }
    public String getCsoDocNo() { return csoDocNo; }
    public void setCsoDocNo(String csoDocNo) { this.csoDocNo = csoDocNo; }
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
    public boolean isRequireGenerateCdo() { return requireGenerateCdo; }
    public void setRequireGenerateCdo(boolean requireGenerateCdo) { this.requireGenerateCdo = requireGenerateCdo; }
    public String getShippingMode() { return shippingMode; }
    public void setShippingMode(String shippingMode) { this.shippingMode = shippingMode; }
    public String getTransporter() { return transporter; }
    public void setTransporter(String transporter) { this.transporter = transporter; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public Instant getReleasedAt() { return releasedAt; }
    public void setReleasedAt(Instant releasedAt) { this.releasedAt = releasedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
