package com.consignment.service.persistence.mapper;

import com.consignment.service.persistence.model.CsoDetailEntity;
import com.consignment.service.persistence.model.CsoHeaderEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.Instant;
import java.util.List;

@Mapper
public interface CsoMapper {

    void insertHeader(CsoHeaderEntity header);

    void insertDetail(CsoDetailEntity detail);

    CsoHeaderEntity findHeaderById(@Param("id") String id);

    List<CsoHeaderEntity> searchHeaders(
            @Param("company") String company,
            @Param("store") String store,
            @Param("customerCode") String customerCode,
            @Param("supplierCode") String supplierCode,
            @Param("supplierContract") String supplierContract,
            @Param("createdMethod") String createdMethod,
            @Param("referenceNo") String referenceNo,
            @Param("itemCode") String itemCode,
            @Param("status") String status
    );

    List<CsoDetailEntity> findDetailsByHeaderId(@Param("csoId") String csoId);

    void updateHeaderStatus(
            @Param("id") String id,
            @Param("status") String status,
            @Param("releasedAt") Instant releasedAt,
            @Param("releasedBy") String releasedBy
    );

    int deleteHeader(@Param("id") String id);

    long countMatchingSetup(
            @Param("itemCode") String itemCode,
            @Param("supplierCode") String supplierCode,
            @Param("supplierContract") String supplierContract,
            @Param("store") String store
    );
}
