package com.consignment.service.persistence.mapper;

import com.consignment.service.persistence.model.CsdoDetailEntity;
import com.consignment.service.persistence.model.CsdoHeaderEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.Instant;
import java.util.List;

@Mapper
public interface CsdoMapper {

    void insertHeader(CsdoHeaderEntity header);

    void insertDetail(CsdoDetailEntity detail);

    CsdoHeaderEntity findHeaderById(@Param("id") String id);

    CsdoHeaderEntity findByCsoId(@Param("csoId") String csoId);

    List<CsdoHeaderEntity> findAllHeaders();

    List<CsdoHeaderEntity> searchHeaders(
            @Param("company") String company,
            @Param("store") String store,
            @Param("customerCode") String customerCode,
            @Param("status") String status,
            @Param("createdMethod") String createdMethod,
            @Param("referenceNo") String referenceNo,
            @Param("itemCode") String itemCode
    );

    List<CsdoDetailEntity> findDetailsByHeaderId(@Param("csdoId") String csdoId);

    void updateHeaderStatus(@Param("id") String id, @Param("status") String status, @Param("releasedAt") Instant releasedAt);
}
