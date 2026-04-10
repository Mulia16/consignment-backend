package com.consignment.service.persistence.model;

import java.math.BigDecimal;

public class CsrnCDetailEntity {
    private String id;
    private String csrnCId;
    private String itemCode;
    private String uom;
    private BigDecimal qty;
    private BigDecimal actualQty;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCsrnCId() { return csrnCId; }
    public void setCsrnCId(String csrnCId) { this.csrnCId = csrnCId; }
    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }
    public String getUom() { return uom; }
    public void setUom(String uom) { this.uom = uom; }
    public BigDecimal getQty() { return qty; }
    public void setQty(BigDecimal qty) { this.qty = qty; }
    public BigDecimal getActualQty() { return actualQty; }
    public void setActualQty(BigDecimal actualQty) { this.actualQty = actualQty; }
}
