package com.consignment.service.persistence.model;

import java.time.Instant;
import java.time.LocalDate;

public class SupplierBillingRequestEntity {
    private String id;
    private String docNo;
    private String company;
    private String periodType;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String store;
    private String supplierCode;
    private String supplierContract;
    private String supplierType;
    private boolean carryForwardDecimal;
    private String status;
    private String processStatus;
    private String errorReason;
    private Instant processDate;
    private String createdBy;
    private Instant releasedAt;
    private Instant createdAt;
    private Instant updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDocNo() { return docNo; }
    public void setDocNo(String docNo) { this.docNo = docNo; }
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
    public String getPeriodType() { return periodType; }
    public void setPeriodType(String periodType) { this.periodType = periodType; }
    public LocalDate getFromDate() { return fromDate; }
    public void setFromDate(LocalDate fromDate) { this.fromDate = fromDate; }
    public LocalDate getToDate() { return toDate; }
    public void setToDate(LocalDate toDate) { this.toDate = toDate; }
    public String getStore() { return store; }
    public void setStore(String store) { this.store = store; }
    public String getSupplierCode() { return supplierCode; }
    public void setSupplierCode(String supplierCode) { this.supplierCode = supplierCode; }
    public String getSupplierContract() { return supplierContract; }
    public void setSupplierContract(String supplierContract) { this.supplierContract = supplierContract; }
    public String getSupplierType() { return supplierType; }
    public void setSupplierType(String supplierType) { this.supplierType = supplierType; }
    public boolean isCarryForwardDecimal() { return carryForwardDecimal; }
    public void setCarryForwardDecimal(boolean carryForwardDecimal) { this.carryForwardDecimal = carryForwardDecimal; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getProcessStatus() { return processStatus; }
    public void setProcessStatus(String processStatus) { this.processStatus = processStatus; }
    public String getErrorReason() { return errorReason; }
    public void setErrorReason(String errorReason) { this.errorReason = errorReason; }
    public Instant getProcessDate() { return processDate; }
    public void setProcessDate(Instant processDate) { this.processDate = processDate; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public Instant getReleasedAt() { return releasedAt; }
    public void setReleasedAt(Instant releasedAt) { this.releasedAt = releasedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
