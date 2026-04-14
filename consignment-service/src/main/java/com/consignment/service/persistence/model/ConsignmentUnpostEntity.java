package com.consignment.service.persistence.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public class ConsignmentUnpostEntity {
    private Long id;
    private String store;
    private String sku;
    private BigDecimal salesQty;
    private BigDecimal salesReturnQty;
    private LocalDate salesDate;
    private String sourceType;
    private String sourceRef;
    private boolean settled;
    private String cbrId;
    private Instant createdAt;
    private Instant updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getStore() { return store; }
    public void setStore(String store) { this.store = store; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public BigDecimal getSalesQty() { return salesQty; }
    public void setSalesQty(BigDecimal salesQty) { this.salesQty = salesQty; }
    public BigDecimal getSalesReturnQty() { return salesReturnQty; }
    public void setSalesReturnQty(BigDecimal salesReturnQty) { this.salesReturnQty = salesReturnQty; }
    public LocalDate getSalesDate() { return salesDate; }
    public void setSalesDate(LocalDate salesDate) { this.salesDate = salesDate; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public String getSourceRef() { return sourceRef; }
    public void setSourceRef(String sourceRef) { this.sourceRef = sourceRef; }
    public boolean isSettled() { return settled; }
    public void setSettled(boolean settled) { this.settled = settled; }
    public String getCbrId() { return cbrId; }
    public void setCbrId(String cbrId) { this.cbrId = cbrId; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
