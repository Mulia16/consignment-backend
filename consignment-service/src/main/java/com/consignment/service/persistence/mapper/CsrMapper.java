package com.consignment.service.persistence.mapper;

import com.consignment.service.model.csr.CsrSearchCriteria;
import com.consignment.service.persistence.model.CsrDetailEntity;
import com.consignment.service.persistence.model.CsrHeaderEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Mapper
public interface CsrMapper {
    void insertHeader(CsrHeaderEntity header);
    void insertDetail(CsrDetailEntity detail);
    CsrHeaderEntity findHeaderById(@Param("id") String id);
    List<CsrHeaderEntity> findAllHeaders();
    List<CsrHeaderEntity> searchHeaders(@Param("c") CsrSearchCriteria criteria);
    long countHeaders(@Param("c") CsrSearchCriteria criteria);
    List<CsrDetailEntity> findDetailsByHeaderId(@Param("csrId") String csrId);
    void updateHeaderStatus(@Param("id") String id, @Param("status") String status,
                            @Param("releasedAt") Instant releasedAt, @Param("completedAt") Instant completedAt);
    void updateActualQty(@Param("detailId") String detailId, @Param("actualQty") BigDecimal actualQty);
}
