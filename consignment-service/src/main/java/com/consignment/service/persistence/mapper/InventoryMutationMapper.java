package com.consignment.service.persistence.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

@Mapper
public interface InventoryMutationMapper {

    void upsertCustomerInventory(
            @Param("issueFromStore") String issueFromStore,
            @Param("customerCode") String customerCode,
            @Param("branchCode") String branchCode,
            @Param("sku") String sku,
            @Param("qtyDelta") BigDecimal qtyDelta
    );

    void adjustSupplierClosing(
            @Param("store") String store,
            @Param("sku") String sku,
            @Param("supplierCode") String supplierCode,
            @Param("supplierContract") String supplierContract,
            @Param("qtyDelta") BigDecimal qtyDelta
    );

    void upsertUnpost(
            @Param("sku") String sku,
            @Param("location") String location,
            @Param("salesDelta") BigDecimal salesDelta,
            @Param("returnDelta") BigDecimal returnDelta
    );
}
