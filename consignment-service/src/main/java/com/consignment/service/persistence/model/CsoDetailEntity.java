package com.consignment.service.persistence.model;

import java.math.BigDecimal;

public class CsoDetailEntity {

    private String id;
    private String csoId;
    private String itemCode;
    private BigDecimal qty;
    private String uom;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCsoId() { return csoId; }
    public void setCsoId(String csoId) { this.csoId = csoId; }
    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }
    public BigDecimal getQty() { return qty; }
    public void setQty(BigDecimal qty) { this.qty = qty; }
    public String getUom() { return uom; }
    public void setUom(String uom) { this.uom = uom; }
}
