package com.consignment.service.persistence.mapper;

import com.consignment.service.persistence.model.ConsignmentUnpostEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ConsignmentUnpostMapper {

    void insert(ConsignmentUnpostEntity entity);

    /** Aggregate unsettled unpost by store+sku for CBR computation */
    List<UnpostAggRow> aggregateUnsettled(
            @Param("store") String store,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    /** Mark unpost rows as settled after CBR release */
    void markSettled(@Param("store") String store,
                     @Param("fromDate") LocalDate fromDate,
                     @Param("toDate") LocalDate toDate,
                     @Param("cbrId") String cbrId);

    /** Check if there's already an unsettled CBR for same store+period to prevent duplicate */
    long countUnsettledCbr(@Param("store") String store,
                           @Param("fromDate") LocalDate fromDate,
                           @Param("toDate") LocalDate toDate);

    record UnpostAggRow(String store, String sku, BigDecimal totalSales, BigDecimal totalReturn) {}
}
