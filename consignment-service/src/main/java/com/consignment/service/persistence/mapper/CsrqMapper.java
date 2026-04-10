package com.consignment.service.persistence.mapper;

import com.consignment.service.model.csrq.CsrqSearchCriteria;
import com.consignment.service.persistence.model.CsrqDetailEntity;
import com.consignment.service.persistence.model.CsrqHeaderEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.Instant;
import java.util.List;

@Mapper
public interface CsrqMapper {
    void insertHeader(CsrqHeaderEntity header);
    void insertDetail(CsrqDetailEntity detail);
    CsrqHeaderEntity findHeaderById(@Param("id") String id);
    List<CsrqHeaderEntity> searchHeaders(@Param("c") CsrqSearchCriteria criteria);
    long countHeaders(@Param("c") CsrqSearchCriteria criteria);
    List<CsrqDetailEntity> findDetailsByHeaderId(@Param("csrqId") String csrqId);
    void updateHeaderStatus(@Param("id") String id, @Param("status") String status, @Param("releasedAt") Instant releasedAt);
    int deleteHeader(@Param("id") String id);
    void updateHeader(CsrqHeaderEntity header);
    void deleteDetails(@Param("csrqId") String csrqId);
    long countMatchingSetup(@Param("itemCode") String itemCode, @Param("supplierCode") String supplierCode,
                            @Param("supplierContract") String supplierContract, @Param("store") String store,
                            @Param("internalSupplierStore") String internalSupplierStore);
    Long findMaxDocNoNumber();
}
