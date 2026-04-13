package com.consignment.service.persistence.mapper;

import com.consignment.service.model.csrn.CsrnSearchCriteria;
import com.consignment.service.persistence.model.CsrnDetailEntity;
import com.consignment.service.persistence.model.CsrnHeaderEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Mapper
public interface CsrnMapper {
    void insertHeader(CsrnHeaderEntity header);
    void insertDetail(CsrnDetailEntity detail);
    CsrnHeaderEntity findHeaderById(@Param("id") String id);
    List<CsrnHeaderEntity> searchHeaders(@Param("c") CsrnSearchCriteria criteria);
    long countHeaders(@Param("c") CsrnSearchCriteria criteria);
    List<CsrnDetailEntity> findDetailsByHeaderId(@Param("csrnId") String csrnId);
    void updateHeaderStatus(@Param("id") String id, @Param("status") String status,
                            @Param("releasedAt") Instant releasedAt, @Param("completedAt") Instant completedAt);
    void updateActualQty(@Param("detailId") String detailId, @Param("actualQty") BigDecimal actualQty);
    void updateHeader(CsrnHeaderEntity header);
    void deleteDetails(@Param("csrnId") String csrnId);
    Long findMaxDocNoNumber();
}
