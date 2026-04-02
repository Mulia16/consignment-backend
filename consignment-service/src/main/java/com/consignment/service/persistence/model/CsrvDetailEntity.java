package com.consignment.service.persistence.model;

import java.math.BigDecimal;

public class CsrvDetailEntity {

    private String id;
    private String csrvId;
    private String itemCode;
    private BigDecimal availableQty;
    private BigDecimal requestQty;
    private BigDecimal receivingQty;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCsrvId() { return csrvId; }
    public void setCsrvId(String csrvId) { this.csrvId = csrvId; }
    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }
    public BigDecimal getAvailableQty() { return availableQty; }
    public void setAvailableQty(BigDecimal availableQty) { this.availableQty = availableQty; }
    public BigDecimal getRequestQty() { return requestQty; }
    public void setRequestQty(BigDecimal requestQty) { this.requestQty = requestQty; }
    public BigDecimal getReceivingQty() { return receivingQty; }
    public void setReceivingQty(BigDecimal receivingQty) { this.receivingQty = receivingQty; }
}
