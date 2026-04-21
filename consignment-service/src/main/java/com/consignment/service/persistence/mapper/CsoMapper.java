package com.consignment.service.persistence.mapper;

import com.consignment.service.model.cso.CsoSearchCriteria;
import com.consignment.service.persistence.model.CsoDetailEntity;
import com.consignment.service.persistence.model.CsoHeaderEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Mapper
public interface CsoMapper {
    void insertHeader(CsoHeaderEntity header);
    void insertDetail(CsoDetailEntity detail);
    CsoHeaderEntity findHeaderById(@Param("id") String id);
    List<CsoHeaderEntity> searchHeaders(@Param("c") CsoSearchCriteria criteria);
    long countHeaders(@Param("c") CsoSearchCriteria criteria);
    List<CsoDetailEntity> findDetailsByHeaderId(@Param("csoId") String csoId);
    void updateHeaderStatus(@Param("id") String id, @Param("status") String status,
                            @Param("releasedAt") Instant releasedAt, @Param("releasedBy") String releasedBy);
    int deleteHeader(@Param("id") String id);
    void updateHeader(CsoHeaderEntity header);
    void deleteDetails(@Param("csoId") String csoId);
    Long findMaxDocNoNumber();
    long countMatchingSetup(@Param("itemCode") String itemCode, @Param("supplierCode") String supplierCode,
                            @Param("supplierContract") String supplierContract, @Param("store") String store);

    /**
     * Returns sales qty per item_code for a given store, combining CSO and CSDO transactions.
     * Each map entry has keys: "item_code" and "sales_qty".
     */
    List<Map<String, Object>> findSalesQtyByStore(@Param("store") String store);
}
