package com.consignment.service.persistence.mapper;

import com.consignment.service.model.setup.ItemSetupSearchCriteria;
import com.consignment.service.persistence.model.ExternalSupplierEntity;
import com.consignment.service.persistence.model.InternalSupplierEntity;
import com.consignment.service.persistence.model.ItemSetupEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ConsignmentSetupMapper {

    List<String> findItemCodesWithSetup();

    List<ItemSetupEntity> searchItems(@Param("criteria") ItemSetupSearchCriteria criteria);

    long countItems(@Param("criteria") ItemSetupSearchCriteria criteria);

    List<ExternalSupplierEntity> findExternalByItemCode(@Param("itemCode") String itemCode);

    List<ExternalSupplierEntity> findExternalByItemCodeAndSupplier(@Param("itemCode") String itemCode,
                                                                    @Param("supplierCode") String supplierCode,
                                                                    @Param("contractNumber") String contractNumber);

    List<InternalSupplierEntity> findInternalByItemCode(@Param("itemCode") String itemCode);

    List<InternalSupplierEntity> findInternalByItemCodeAndSupplier(@Param("itemCode") String itemCode,
                                                                    @Param("supplierCode") String supplierCode,
                                                                    @Param("supplierStore") String supplierStore);

    ExternalSupplierEntity findExternalById(@Param("itemCode") String itemCode, @Param("id") String id);

    void insertExternal(ExternalSupplierEntity entity);

    void updateExternal(ExternalSupplierEntity entity);

    int deleteExternal(@Param("itemCode") String itemCode, @Param("id") String id);

    int deleteExternalBySupplier(@Param("itemCode") String itemCode,
                                  @Param("supplierCode") String supplierCode,
                                  @Param("contractNumber") String contractNumber);

    void insertInternal(InternalSupplierEntity entity);

    int deleteInternalBySupplier(@Param("itemCode") String itemCode,
                                  @Param("supplierCode") String supplierCode,
                                  @Param("supplierStore") String supplierStore);
}
