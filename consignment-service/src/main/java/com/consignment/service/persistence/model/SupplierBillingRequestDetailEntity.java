package com.consignment.service.persistence.model;

import java.math.BigDecimal;

public class SupplierBillingRequestDetailEntity {
    private String id;
    private String billingId;
    private String itemCode;
    private String uom;
    private BigDecimal salesQty;
    private BigDecimal salesReturnQty;
    private BigDecimal bfQty;
    private BigDecimal billingQty;
    private BigDecimal cfQty;
    private BigDecimal unitCost;
    private BigDecimal totalCost;
    private BigDecimal totalSupplierQty;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getBillingId() { return billingId; }
    public void setBillingId(String billingId) { this.billingId = billingId; }
    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }
    public String getUom() { return uom; }
    public void setUom(String uom) { this.uom = uom; }
    public BigDecimal getSalesQty() { return salesQty; }
    public void setSalesQty(BigDecimal salesQty) { this.salesQty = salesQty; }
    public BigDecimal getSalesReturnQty() { return salesReturnQty; }
    public void setSalesReturnQty(BigDecimal salesReturnQty) { this.salesReturnQty = salesReturnQty; }
    public BigDecimal getBfQty() { return bfQty; }
    public void setBfQty(BigDecimal bfQty) { this.bfQty = bfQty; }
    public BigDecimal getBillingQty() { return billingQty; }
    public void setBillingQty(BigDecimal billingQty) { this.billingQty = billingQty; }
    public BigDecimal getCfQty() { return cfQty; }
    public void setCfQty(BigDecimal cfQty) { this.cfQty = cfQty; }
    public BigDecimal getUnitCost() { return unitCost; }
    public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }
    public BigDecimal getTotalCost() { return totalCost; }
    public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }
    public BigDecimal getTotalSupplierQty() { return totalSupplierQty; }
    public void setTotalSupplierQty(BigDecimal totalSupplierQty) { this.totalSupplierQty = totalSupplierQty; }
}
