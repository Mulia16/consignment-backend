package com.consignment.service.persistence.mapper;

import com.consignment.service.model.billing.SupplierBillingSearchCriteria;
import com.consignment.service.persistence.model.SupplierBillingRequestDetailEntity;
import com.consignment.service.persistence.model.SupplierBillingRequestEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Mapper
public interface SupplierBillingMapper {

    void insertHeader(SupplierBillingRequestEntity header);
    void insertDetail(SupplierBillingRequestDetailEntity detail);

    SupplierBillingRequestEntity findById(@Param("id") String id);
    List<SupplierBillingRequestEntity> search(@Param("c") SupplierBillingSearchCriteria criteria);
    long count(@Param("c") SupplierBillingSearchCriteria criteria);
    List<SupplierBillingRequestDetailEntity> findDetailsByBillingId(@Param("billingId") String billingId);

    void updateStatus(@Param("id") String id, @Param("status") String status,
                      @Param("releasedAt") Instant releasedAt);
    void deleteById(@Param("id") String id);
    Long findMaxDocNoNumber();

    /** Get CF_Qty from the most recent RELEASED SCBR detail for BF_Qty carry-forward */
    BigDecimal findLastCfQty(@Param("store") String store,
                             @Param("supplierCode") String supplierCode,
                             @Param("itemCode") String itemCode);
}
