package com.consignment.service.persistence.mapper;

import com.consignment.service.model.billing.CustomerBillingSearchCriteria;
import com.consignment.service.persistence.model.CustomerBillingRequestDetailEntity;
import com.consignment.service.persistence.model.CustomerBillingRequestEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface CustomerBillingMapper {
    void insertHeader(CustomerBillingRequestEntity header);
    void insertDetail(CustomerBillingRequestDetailEntity detail);

    CustomerBillingRequestEntity findById(@Param("id") String id);
    List<CustomerBillingRequestEntity> search(@Param("c") CustomerBillingSearchCriteria criteria);
    long count(@Param("c") CustomerBillingSearchCriteria criteria);
    List<CustomerBillingRequestDetailEntity> findDetailsByBillingId(@Param("billingId") String billingId);

    void updateStatus(@Param("id") String id, @Param("status") String status,
                      @Param("releasedAt") Instant releasedAt);
    void updateActualReturnQty(@Param("detailId") String detailId,
                               @Param("actualReturnQty") BigDecimal actualReturnQty);
    void deleteById(@Param("id") String id);
    Long findMaxDocNoNumber();

    /** Query unpost_staging_inventory for a store+customer to compute sales/return qty */
    List<UnpostRow> findUnpostByStoreAndCustomer(
            @Param("store") String store,
            @Param("customerCode") String customerCode,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );

    record UnpostRow(String sku, String location, BigDecimal salesQty, BigDecimal returnQty) {}
}
