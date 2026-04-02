package com.consignment.service.persistence.mapper;

import com.consignment.service.persistence.model.ItemSetupEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ItemSetupMapper {

    ItemSetupEntity findByItemCode(@Param("itemCode") String itemCode);

    void upsert(ItemSetupEntity entity);

    void ensureExists(@Param("itemCode") String itemCode, @Param("hierarchy") String hierarchy);
}
