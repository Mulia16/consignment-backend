package com.consignment.service.persistence.model;

import java.time.Instant;
import java.time.LocalDate;

public class CustomerBillingRequestEntity {
    private String id;
    private String docNo;
    private String periodType;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String store;
    private String customerCode;
    private String customerBranch;
    private String status;
    private String processStatus;
    private String createdBy;
    private Instant releasedAt;
    private Instant createdAt;
    private Instant updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDocNo() { return docNo; }
    public void setDocNo(String docNo) { this.docNo = docNo; }
    public String getPeriodType() { return periodType; }
    public void setPeriodType(String periodType) { this.periodType = periodType; }
    public LocalDate getFromDate() { return fromDate; }
    public void setFromDate(LocalDate fromDate) { this.fromDate = fromDate; }
    public LocalDate getToDate() { return toDate; }
    public void setToDate(LocalDate toDate) { this.toDate = toDate; }
    public String getStore() { return store; }
    public void setStore(String store) { this.store = store; }
    public String getCustomerCode() { return customerCode; }
    public void setCustomerCode(String customerCode) { this.customerCode = customerCode; }
    public String getCustomerBranch() { return customerBranch; }
    public void setCustomerBranch(String customerBranch) { this.customerBranch = customerBranch; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getProcessStatus() { return processStatus; }
    public void setProcessStatus(String processStatus) { this.processStatus = processStatus; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public Instant getReleasedAt() { return releasedAt; }
    public void setReleasedAt(Instant releasedAt) { this.releasedAt = releasedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
