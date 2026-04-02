package com.consignment.service.persistence.model;

import java.math.BigDecimal;

public class CsrqDetailEntity {

    private String id;
    private String csrqId;
    private String itemCode;
    private BigDecimal requestQty;
    private String requestUom;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCsrqId() {
        return csrqId;
    }

    public void setCsrqId(String csrqId) {
        this.csrqId = csrqId;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public BigDecimal getRequestQty() {
        return requestQty;
    }

    public void setRequestQty(BigDecimal requestQty) {
        this.requestQty = requestQty;
    }

    public String getRequestUom() {
        return requestUom;
    }

    public void setRequestUom(String requestUom) {
        this.requestUom = requestUom;
    }
}
