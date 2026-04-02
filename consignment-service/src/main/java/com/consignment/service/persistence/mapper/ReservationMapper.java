package com.consignment.service.persistence.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

@Mapper
public interface ReservationMapper {

    void insertReservation(
            @Param("docNo") String docNo,
            @Param("docType") String docType,
            @Param("store") String store,
            @Param("sku") String sku,
            @Param("qty") BigDecimal qty,
            @Param("reservationType") String reservationType
    );

    int deleteByDocNo(@Param("docNo") String docNo);
}
