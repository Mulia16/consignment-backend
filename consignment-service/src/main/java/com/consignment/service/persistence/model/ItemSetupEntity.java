package com.consignment.service.persistence.model;

import java.time.Instant;

public class ItemSetupEntity {

    private Long id;
    private String itemCode;
    private String itemName;
    private String variant;
    private String hierarchy;
    private String itemModel;
    private java.math.BigDecimal unitRetail;
    private java.math.BigDecimal mvc;
    private String categoryL1;
    private String categoryL2;
    private String categoryL3;
    private boolean syncFlag;
    private boolean deletedFlag;
    private Instant createdAt;
    private Instant updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getVariant() { return variant; }
    public void setVariant(String variant) { this.variant = variant; }

    public String getHierarchy() { return hierarchy; }
    public void setHierarchy(String hierarchy) { this.hierarchy = hierarchy; }

    public String getItemModel() { return itemModel; }
    public void setItemModel(String itemModel) { this.itemModel = itemModel; }

    public java.math.BigDecimal getUnitRetail() { return unitRetail; }
    public void setUnitRetail(java.math.BigDecimal unitRetail) { this.unitRetail = unitRetail; }

    public java.math.BigDecimal getMvc() { return mvc; }
    public void setMvc(java.math.BigDecimal mvc) { this.mvc = mvc; }

    public String getCategoryL1() { return categoryL1; }
    public void setCategoryL1(String categoryL1) { this.categoryL1 = categoryL1; }

    public String getCategoryL2() { return categoryL2; }
    public void setCategoryL2(String categoryL2) { this.categoryL2 = categoryL2; }

    public String getCategoryL3() { return categoryL3; }
    public void setCategoryL3(String categoryL3) { this.categoryL3 = categoryL3; }

    public boolean isSyncFlag() { return syncFlag; }
    public void setSyncFlag(boolean syncFlag) { this.syncFlag = syncFlag; }

    public boolean isDeletedFlag() { return deletedFlag; }
    public void setDeletedFlag(boolean deletedFlag) { this.deletedFlag = deletedFlag; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
