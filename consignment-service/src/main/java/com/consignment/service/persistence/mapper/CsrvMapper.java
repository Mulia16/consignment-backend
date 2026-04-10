package com.consignment.service.persistence.mapper;

import com.consignment.service.model.csrv.CsrvSearchCriteria;
import com.consignment.service.persistence.model.CsrvDetailEntity;
import com.consignment.service.persistence.model.CsrvHeaderEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.Instant;
import java.util.List;

@Mapper
public interface CsrvMapper {
    void insertHeader(CsrvHeaderEntity header);
    void insertDetail(CsrvDetailEntity detail);
    CsrvHeaderEntity findHeaderById(@Param("id") String id);
    List<CsrvHeaderEntity> searchHeaders(@Param("c") CsrvSearchCriteria criteria);
    long countHeaders(@Param("c") CsrvSearchCriteria criteria);
    List<CsrvDetailEntity> findDetailsByHeaderId(@Param("csrvId") String csrvId);
    void updateHeaderStatus(@Param("id") String id, @Param("status") String status, @Param("releasedAt") Instant releasedAt);
    long countMatchingSetup(@Param("itemCode") String itemCode, @Param("supplierCode") String supplierCode,
                            @Param("supplierContract") String supplierContract, @Param("receivingStore") String receivingStore);
    Long findMaxDocNoNumber();
}
