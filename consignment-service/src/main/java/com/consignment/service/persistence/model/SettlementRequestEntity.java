package com.consignment.service.persistence.model;

import java.time.Instant;
import java.math.BigDecimal;

public class SettlementRequestEntity {
    private String id;
    private String docNo;
    private String company;
    private String store;
    private String settlementType;  // CUSTOMER, SUPPLIER
    private String customerCode;
    private String supplierCode;
    private String supplierContract;
    private BigDecimal totalAmount;
    private String currency;
    private String status;  // HELD, READY_FOR_BILLING, BILLED, SETTLED
    private String createdBy;
    private String referenceNo;
    private String remark;
    private Instant readyForBillingAt;
    private Instant billedAt;
    private Instant settledAt;
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
    public String getSettlementType() { return settlementType; }
    public void setSettlementType(String settlementType) { this.settlementType = settlementType; }
    public String getCustomerCode() { return customerCode; }
    public void setCustomerCode(String customerCode) { this.customerCode = customerCode; }
    public String getSupplierCode() { return supplierCode; }
    public void setSupplierCode(String supplierCode) { this.supplierCode = supplierCode; }
    public String getSupplierContract() { return supplierContract; }
    public void setSupplierContract(String supplierContract) { this.supplierContract = supplierContract; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public String getReferenceNo() { return referenceNo; }
    public void setReferenceNo(String referenceNo) { this.referenceNo = referenceNo; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Instant getReadyForBillingAt() { return readyForBillingAt; }
    public void setReadyForBillingAt(Instant readyForBillingAt) { this.readyForBillingAt = readyForBillingAt; }
    public Instant getBilledAt() { return billedAt; }
    public void setBilledAt(Instant billedAt) { this.billedAt = billedAt; }
    public Instant getSettledAt() { return settledAt; }
    public void setSettledAt(Instant settledAt) { this.settledAt = settledAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
