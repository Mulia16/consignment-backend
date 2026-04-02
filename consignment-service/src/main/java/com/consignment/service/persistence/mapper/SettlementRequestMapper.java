package com.consignment.service.persistence.mapper;

import com.consignment.service.persistence.model.SettlementRequestEntity;
import com.consignment.service.persistence.model.SettlementDetailEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Mapper
public interface SettlementRequestMapper {

    void insertHeader(SettlementRequestEntity header);

    void insertDetail(SettlementDetailEntity detail);

    SettlementRequestEntity findHeaderById(@Param("id") String id);

    List<SettlementRequestEntity> findAllHeaders();

    List<SettlementRequestEntity> searchHeaders(
            @Param("company") String company,
            @Param("store") String store,
            @Param("settlementType") String settlementType,
            @Param("customerCode") String customerCode,
            @Param("supplierCode") String supplierCode,
            @Param("status") String status,
            @Param("createdBy") String createdBy
    );

    List<SettlementDetailEntity> findDetailsByHeaderId(@Param("settlementId") String settlementId);

    int countOpenByReference(
            @Param("company") String company,
            @Param("store") String store,
            @Param("settlementType") String settlementType,
            @Param("referenceNo") String referenceNo
    );

    int countExistingDetail(
            @Param("settlementId") String settlementId,
            @Param("documentType") String documentType,
            @Param("documentNo") String documentNo,
            @Param("itemCode") String itemCode
    );

    void updateTotalAmount(@Param("id") String id, @Param("totalAmount") BigDecimal totalAmount);

    void updateHeaderStatus(
            @Param("id") String id,
            @Param("status") String status,
            @Param("readyForBillingAt") Instant readyForBillingAt,
            @Param("billedAt") Instant billedAt,
            @Param("settledAt") Instant settledAt
    );
}
