package com.consignment.service.persistence.mapper;

import com.consignment.service.persistence.model.CsaDetailEntity;
import com.consignment.service.persistence.model.CsaHeaderEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.Instant;
import java.util.List;

@Mapper
public interface CsaMapper {

    void insertHeader(CsaHeaderEntity header);

    void insertDetail(CsaDetailEntity detail);

    CsaHeaderEntity findHeaderById(@Param("id") String id);

    List<CsaHeaderEntity> findAllHeaders();

    List<CsaHeaderEntity> searchHeaders(
            @Param("company") String company,
            @Param("store") String store,
            @Param("transactionType") String transactionType,
            @Param("status") String status,
            @Param("createdBy") String createdBy,
            @Param("referenceNo") String referenceNo
    );

    List<CsaDetailEntity> findDetailsByHeaderId(@Param("csaId") String csaId);

    void updateHeaderStatus(
            @Param("id") String id,
            @Param("status") String status,
            @Param("releasedAt") Instant releasedAt,
            @Param("releasedBy") String releasedBy
    );
}
