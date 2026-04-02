package com.consignment.service.persistence.mapper;

import com.consignment.service.persistence.model.ItemPriceEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ItemPriceMapper {

    void upsert(ItemPriceEntity entity);

    ItemPriceEntity findEffectivePrice(
            @Param("itemCode") String itemCode,
            @Param("company") String company,
            @Param("store") String store,
            @Param("supplierCode") String supplierCode,
            @Param("supplierContract") String supplierContract,
            @Param("customerCode") String customerCode
    );
}