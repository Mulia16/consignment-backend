package com.consignment.service.persistence.mapper;

import com.consignment.service.model.csrn.CsrnCSearchCriteria;
import com.consignment.service.model.csrn.CsrnSearchCriteria;
import com.consignment.service.persistence.model.CsrnCDetailEntity;
import com.consignment.service.persistence.model.CsrnCHeaderEntity;
import com.consignment.service.persistence.model.CsrnDetailEntity;
import com.consignment.service.persistence.model.CsrnHeaderEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CsrnMapper {
    // CSRN
    void insertHeader(CsrnHeaderEntity header);
    void insertDetail(CsrnDetailEntity detail);
    void deleteDetails(@Param("csrnId") String csrnId);
    CsrnHeaderEntity findHeaderById(@Param("id") String id);
    List<CsrnHeaderEntity> searchHeaders(@Param("c") CsrnSearchCriteria criteria);
    long countHeaders(@Param("c") CsrnSearchCriteria criteria);
    List<CsrnDetailEntity> findDetailsByHeaderId(@Param("csrnId") String csrnId);
    void updateStatus(@Param("id") String id, @Param("status") String status);
    Long findMaxDocNoNumber();

    // CSRN-C
    void insertCHeader(CsrnCHeaderEntity header);
    void insertCDetail(CsrnCDetailEntity detail);
    CsrnCHeaderEntity findCHeaderById(@Param("id") String id);
    CsrnCHeaderEntity findCHeaderByCsrnId(@Param("csrnId") String csrnId);
    List<CsrnCHeaderEntity> searchCHeaders(@Param("c") CsrnCSearchCriteria criteria);
    long countCHeaders(@Param("c") CsrnCSearchCriteria criteria);
    List<CsrnCDetailEntity> findCDetailsByCHeaderId(@Param("csrnCId") String csrnCId);
    Long findMaxCDocNoNumber();
}
