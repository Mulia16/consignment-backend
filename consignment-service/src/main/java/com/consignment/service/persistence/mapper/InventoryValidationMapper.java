package com.consignment.service.persistence.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface InventoryValidationMapper {

    long countBlockingInventory(@Param("itemCode") String itemCode, @Param("storeCode") String storeCode);
}
