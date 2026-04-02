package com.consignment.service.persistence.mapper;

import com.consignment.service.persistence.model.CsrqDetailEntity;
import com.consignment.service.persistence.model.CsrqHeaderEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CsrqMapper {

    void insertHeader(CsrqHeaderEntity header);

    void insertDetail(CsrqDetailEntity detail);

    CsrqHeaderEntity findHeaderById(@Param("id") String id);

    List<CsrqHeaderEntity> searchHeaders(
            @Param("company") String company,
            @Param("store") String store,
            @Param("supplierCode") String supplierCode,
            @Param("supplierContract") String supplierContract,
            @Param("branch") String branch,
            @Param("internalSupplierStore") String internalSupplierStore,
            @Param("createdMethod") String createdMethod,
            @Param("referenceNo") String referenceNo,
            @Param("itemCode") String itemCode,
            @Param("status") String status
    );

    List<CsrqDetailEntity> findDetailsByHeaderId(@Param("csrqId") String csrqId);

    void updateHeaderStatus(
            @Param("id") String id,
            @Param("status") String status,
            @Param("releasedAt") java.time.Instant releasedAt
    );

    int deleteHeader(@Param("id") String id);

    long countMatchingSetup(
            @Param("itemCode") String itemCode,
            @Param("supplierCode") String supplierCode,
            @Param("supplierContract") String supplierContract,
            @Param("store") String store,
            @Param("internalSupplierStore") String internalSupplierStore
    );
}
