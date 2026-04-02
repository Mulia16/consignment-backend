package com.consignment.service.persistence.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;

public class ItemPriceEntity {

    private String id;
    private String itemCode;
    private String company;
    private String store;
    private String supplierCode;
    private String supplierContract;
    private String customerCode;
    private BigDecimal unitPrice;
    private String currency;
    private LocalDate effectiveFrom;
    private Instant createdAt;
    private Instant updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
    public String getStore() { return store; }
    public void setStore(String store) { this.store = store; }
    public String getSupplierCode() { return supplierCode; }
    public void setSupplierCode(String supplierCode) { this.supplierCode = supplierCode; }
    public String getSupplierContract() { return supplierContract; }
    public void setSupplierContract(String supplierContract) { this.supplierContract = supplierContract; }
    public String getCustomerCode() { return customerCode; }
    public void setCustomerCode(String customerCode) { this.customerCode = customerCode; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public LocalDate getEffectiveFrom() { return effectiveFrom; }
    public void setEffectiveFrom(LocalDate effectiveFrom) { this.effectiveFrom = effectiveFrom; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}