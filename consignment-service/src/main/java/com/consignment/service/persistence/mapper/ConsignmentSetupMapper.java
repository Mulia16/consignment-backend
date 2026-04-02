package com.consignment.service.persistence.mapper;

import com.consignment.service.persistence.model.ExternalSupplierEntity;
import com.consignment.service.persistence.model.InternalSupplierEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ConsignmentSetupMapper {

    List<String> findItemCodesWithSetup();

    List<ExternalSupplierEntity> findExternalByItemCode(@Param("itemCode") String itemCode);

    List<InternalSupplierEntity> findInternalByItemCode(@Param("itemCode") String itemCode);

    ExternalSupplierEntity findExternalById(@Param("itemCode") String itemCode, @Param("id") String id);

    void insertExternal(ExternalSupplierEntity entity);

    void updateExternal(ExternalSupplierEntity entity);

    int deleteExternal(@Param("itemCode") String itemCode, @Param("id") String id);

    void insertInternal(InternalSupplierEntity entity);
}
