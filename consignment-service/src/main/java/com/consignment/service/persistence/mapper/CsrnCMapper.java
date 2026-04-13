package com.consignment.service.persistence.mapper;

import com.consignment.service.model.csrn.CsrnCSearchCriteria;
import com.consignment.service.persistence.model.CsrnCDetailEntity;
import com.consignment.service.persistence.model.CsrnCHeaderEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface CsrnCMapper {
    void insertCHeader(CsrnCHeaderEntity header);
    void insertCDetail(CsrnCDetailEntity detail);
    CsrnCHeaderEntity findCHeaderById(@Param("id") String id);
    List<CsrnCHeaderEntity> searchCHeaders(@Param("c") CsrnCSearchCriteria criteria);
    long countCHeaders(@Param("c") CsrnCSearchCriteria criteria);
    List<CsrnCDetailEntity> findCDetailsByCHeaderId(@Param("csrnCId") String csrnCId);
    void updateCActualQty(@Param("detailId") String detailId, @Param("actualQty") BigDecimal actualQty);
    void updateCStatus(@Param("id") String id, @Param("status") String status);
    Long findMaxCDocNoNumber();
}
