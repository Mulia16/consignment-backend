package com.consignment.service.persistence.model;

import java.math.BigDecimal;

public class CsaDetailEntity {

    private String id;
    private String csaId;
    private String itemCode;
    private String itemName;
    private BigDecimal qty;
    private String uom;
    private String settlementDecision;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCsaId() { return csaId; }
    public void setCsaId(String csaId) { this.csaId = csaId; }
    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public BigDecimal getQty() { return qty; }
    public void setQty(BigDecimal qty) { this.qty = qty; }
    public String getUom() { return uom; }
    public void setUom(String uom) { this.uom = uom; }
    public String getSettlementDecision() { return settlementDecision; }
    public void setSettlementDecision(String settlementDecision) { this.settlementDecision = settlementDecision; }
}
