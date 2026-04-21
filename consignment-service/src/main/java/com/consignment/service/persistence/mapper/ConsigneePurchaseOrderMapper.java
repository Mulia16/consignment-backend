package com.consignment.service.persistence.mapper;

import com.consignment.service.persistence.model.ConsigneePurchaseOrderEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ConsigneePurchaseOrderMapper {

    List<ConsigneePurchaseOrderEntity> findByStore(@Param("store") String store);
}
