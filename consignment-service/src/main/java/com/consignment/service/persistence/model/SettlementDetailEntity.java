package com.consignment.service.persistence.model;

import java.time.Instant;
import java.math.BigDecimal;

public class SettlementDetailEntity {
    private String id;
    private String settlementId;
    private String documentType;  // CSO, CSRV, CSR
    private String documentNo;
    private String itemCode;
    private BigDecimal qty;
    private String uom;
    private BigDecimal unitPrice;
    private BigDecimal lineAmount;
    private String remark;
    private Instant createdAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSettlementId() { return settlementId; }
    public void setSettlementId(String settlementId) { this.settlementId = settlementId; }
    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }
    public String getDocumentNo() { return documentNo; }
    public void setDocumentNo(String documentNo) { this.documentNo = documentNo; }
    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }
    public BigDecimal getQty() { return qty; }
    public void setQty(BigDecimal qty) { this.qty = qty; }
    public String getUom() { return uom; }
    public void setUom(String uom) { this.uom = uom; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getLineAmount() { return lineAmount; }
    public void setLineAmount(BigDecimal lineAmount) { this.lineAmount = lineAmount; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
