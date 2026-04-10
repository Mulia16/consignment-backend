package com.consignment.service.persistence.model;

import java.math.BigDecimal;

public class CsrnDetailEntity {
    private String id;
    private String csrnId;
    private String itemCode;
    private String uom;
    private BigDecimal qty;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCsrnId() { return csrnId; }
    public void setCsrnId(String csrnId) { this.csrnId = csrnId; }
    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }
    public String getUom() { return uom; }
    public void setUom(String uom) { this.uom = uom; }
    public BigDecimal getQty() { return qty; }
    public void setQty(BigDecimal qty) { this.qty = qty; }
}
