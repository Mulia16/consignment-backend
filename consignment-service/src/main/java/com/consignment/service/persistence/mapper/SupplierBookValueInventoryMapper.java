package com.consignment.service.persistence.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

@Mapper
public interface SupplierBookValueInventoryMapper {

    void upsertReceiving(
            @Param("store") String store,
            @Param("sku") String sku,
            @Param("supplierCode") String supplierCode,
            @Param("supplierContract") String supplierContract,
            @Param("receivingQty") BigDecimal receivingQty
    );
}
