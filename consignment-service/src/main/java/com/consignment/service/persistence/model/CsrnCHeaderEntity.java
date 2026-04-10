package com.consignment.service.persistence.model;

import java.time.Instant;

public class CsrnCHeaderEntity {
    private String id;
    private String docNo;
    private String csrnId;
    private String csrnDocNo;
    private String csoDocNo;
    private String company;
    private String store;
    private String supplierCode;
    private String supplierContract;
    private String reasonCode;
    private String remark;
    private String createdBy;
    private Instant createdAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDocNo() { return docNo; }
    public void setDocNo(String docNo) { this.docNo = docNo; }
    public String getCsrnId() { return csrnId; }
    public void setCsrnId(String csrnId) { this.csrnId = csrnId; }
    public String getCsrnDocNo() { return csrnDocNo; }
    public void setCsrnDocNo(String csrnDocNo) { this.csrnDocNo = csrnDocNo; }
    public String getCsoDocNo() { return csoDocNo; }
    public void setCsoDocNo(String csoDocNo) { this.csoDocNo = csoDocNo; }
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
    public String getStore() { return store; }
    public void setStore(String store) { this.store = store; }
    public String getSupplierCode() { return supplierCode; }
    public void setSupplierCode(String supplierCode) { this.supplierCode = supplierCode; }
    public String getSupplierContract() { return supplierContract; }
    public void setSupplierContract(String supplierContract) { this.supplierContract = supplierContract; }
    public String getReasonCode() { return reasonCode; }
    public void setReasonCode(String reasonCode) { this.reasonCode = reasonCode; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
