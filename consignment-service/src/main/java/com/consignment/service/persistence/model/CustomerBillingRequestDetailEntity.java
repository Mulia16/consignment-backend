package com.consignment.service.persistence.model;

import java.math.BigDecimal;

public class CustomerBillingRequestDetailEntity {
    private String id;
    private String billingId;
    private String customerCode;
    private String customerBranch;
    private String itemCode;
    private String uom;
    private BigDecimal salesQty;
    private BigDecimal returnQty;
    private BigDecimal billingQty;
    private BigDecimal unitPrice;
    private BigDecimal lineAmount;
    private BigDecimal actualReturnQty;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getBillingId() { return billingId; }
    public void setBillingId(String billingId) { this.billingId = billingId; }
    public String getCustomerCode() { return customerCode; }
    public void setCustomerCode(String customerCode) { this.customerCode = customerCode; }
    public String getCustomerBranch() { return customerBranch; }
    public void setCustomerBranch(String customerBranch) { this.customerBranch = customerBranch; }
    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }
    public String getUom() { return uom; }
    public void setUom(String uom) { this.uom = uom; }
    public BigDecimal getSalesQty() { return salesQty; }
    public void setSalesQty(BigDecimal salesQty) { this.salesQty = salesQty; }
    public BigDecimal getReturnQty() { return returnQty; }
    public void setReturnQty(BigDecimal returnQty) { this.returnQty = returnQty; }
    public BigDecimal getBillingQty() { return billingQty; }
    public void setBillingQty(BigDecimal billingQty) { this.billingQty = billingQty; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getLineAmount() { return lineAmount; }
    public void setLineAmount(BigDecimal lineAmount) { this.lineAmount = lineAmount; }
    public BigDecimal getActualReturnQty() { return actualReturnQty; }
    public void setActualReturnQty(BigDecimal actualReturnQty) { this.actualReturnQty = actualReturnQty; }
}
