package com.consignment.service.persistence.mapper;

import com.consignment.service.model.csa.CsaSearchCriteria;
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
    List<CsaHeaderEntity> searchHeaders(@Param("c") CsaSearchCriteria criteria);
    long countHeaders(@Param("c") CsaSearchCriteria criteria);
    List<CsaDetailEntity> findDetailsByHeaderId(@Param("csaId") String csaId);
    void updateHeaderStatus(@Param("id") String id, @Param("status") String status,
                            @Param("releasedAt") Instant releasedAt, @Param("releasedBy") String releasedBy);
    Long findMaxDocNoNumber();
}
