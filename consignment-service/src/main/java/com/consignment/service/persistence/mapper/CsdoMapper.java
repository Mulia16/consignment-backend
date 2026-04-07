package com.consignment.service.persistence.mapper;

import com.consignment.service.model.csdo.CsdoSearchCriteria;
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
    List<CsdoHeaderEntity> searchHeaders(@Param("c") CsdoSearchCriteria criteria);
    long countHeaders(@Param("c") CsdoSearchCriteria criteria);
    List<CsdoDetailEntity> findDetailsByHeaderId(@Param("csdoId") String csdoId);
    void updateHeaderStatus(@Param("id") String id, @Param("status") String status, @Param("releasedAt") Instant releasedAt);
}
