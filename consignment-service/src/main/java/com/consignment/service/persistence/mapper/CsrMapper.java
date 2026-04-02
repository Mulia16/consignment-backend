package com.consignment.service.persistence.mapper;

import com.consignment.service.persistence.model.CsrDetailEntity;
import com.consignment.service.persistence.model.CsrHeaderEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.Instant;
import java.util.List;

@Mapper
public interface CsrMapper {

    void insertHeader(CsrHeaderEntity header);

    void insertDetail(CsrDetailEntity detail);

    CsrHeaderEntity findHeaderById(@Param("id") String id);

    List<CsrHeaderEntity> findAllHeaders();

    List<CsrHeaderEntity> searchHeaders(
            @Param("company") String company,
            @Param("store") String store,
            @Param("supplierCode") String supplierCode,
            @Param("supplierContract") String supplierContract,
            @Param("status") String status,
            @Param("createdBy") String createdBy,
            @Param("referenceNo") String referenceNo,
            @Param("itemCode") String itemCode
    );

    List<CsrDetailEntity> findDetailsByHeaderId(@Param("csrId") String csrId);

    void updateHeaderStatus(
            @Param("id") String id,
            @Param("status") String status,
            @Param("releasedAt") Instant releasedAt,
            @Param("completedAt") Instant completedAt
    );

    void updateActualQty(@Param("detailId") String detailId, @Param("actualQty") java.math.BigDecimal actualQty);
}
