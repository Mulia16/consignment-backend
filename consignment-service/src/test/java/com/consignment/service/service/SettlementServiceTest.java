package com.consignment.service.service;

import com.consignment.service.exception.BusinessRuleViolationException;
import com.consignment.service.model.settlement.SettlementBatchGenerateRequest;
import com.consignment.service.model.settlement.SettlementDocumentPostRequest;
import com.consignment.service.model.settlement.SettlementDocumentSourceRequest;
import com.consignment.service.persistence.mapper.CsaMapper;
import com.consignment.service.persistence.mapper.CsoMapper;
import com.consignment.service.persistence.mapper.CsrMapper;
import com.consignment.service.persistence.mapper.CsrvMapper;
import com.consignment.service.persistence.mapper.CsdoMapper;
import com.consignment.service.persistence.mapper.SettlementRequestMapper;
import com.consignment.service.persistence.model.CsoDetailEntity;
import com.consignment.service.persistence.model.CsoHeaderEntity;
import com.consignment.service.persistence.model.SettlementDetailEntity;
import com.consignment.service.persistence.model.SettlementRequestEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SettlementServiceTest {

    @Mock
    private SettlementRequestMapper settlementRequestMapper;

    @Mock
    private CsoMapper csoMapper;

    @Mock
    private CsdoMapper csdoMapper;

    @Mock
    private CsrvMapper csrvMapper;

    @Mock
    private CsrMapper csrMapper;

    @Mock
    private CsaMapper csaMapper;

    @Mock
    private PricingService pricingService;

    private SettlementService settlementService;

    @BeforeEach
    void setUp() {
        settlementService = new SettlementService(
                settlementRequestMapper,
                csoMapper,
                csdoMapper,
                csrvMapper,
                csrMapper,
                csaMapper,
                pricingService
        );
    }

    @Test
    void shouldRejectSupplierDocumentForCustomerSettlement() {
        SettlementRequestEntity header = settlementHeader("SET-1", "CUSTOMER", "HELD");
        when(settlementRequestMapper.findHeaderById("SET-1")).thenReturn(header);

        SettlementDocumentPostRequest request = new SettlementDocumentPostRequest(List.of(
                new SettlementDocumentSourceRequest("CSRV", "CSRV-1", BigDecimal.ONE, null)
        ));

        assertThrows(BusinessRuleViolationException.class, () -> settlementService.postDetailsFromDocuments("SET-1", request));
        verify(settlementRequestMapper, never()).insertDetail(any());
    }

    @Test
    void shouldInsertCsoDetailsOnlyOncePerSettlementDocumentLine() {
        SettlementRequestEntity settlement = settlementHeader("SET-1", "CUSTOMER", "HELD");
        CsoHeaderEntity csoHeader = new CsoHeaderEntity();
        csoHeader.setId("CSO-H-1");
        csoHeader.setDocNo("CSO-00001");
        csoHeader.setCompany("COMP01");
        csoHeader.setStore("STORE01");
        csoHeader.setCustomerCode("CUST01");
        csoHeader.setStatus("RELEASED");

        CsoDetailEntity csoDetail = new CsoDetailEntity();
        csoDetail.setItemCode("SKU-1");
        csoDetail.setQty(new BigDecimal("3"));
        csoDetail.setUom("PCS");

        when(settlementRequestMapper.findHeaderById("SET-1")).thenReturn(settlement);
        when(csoMapper.findHeaderById("CSO-H-1")).thenReturn(csoHeader);
        when(csoMapper.findDetailsByHeaderId("CSO-H-1")).thenReturn(List.of(csoDetail));
        when(settlementRequestMapper.countExistingDetail("SET-1", "CSO", "CSO-00001", "SKU-1"))
                .thenReturn(0)
                .thenReturn(1);
        when(settlementRequestMapper.findDetailsByHeaderId("SET-1")).thenReturn(List.of());

        SettlementDocumentPostRequest request = new SettlementDocumentPostRequest(List.of(
                new SettlementDocumentSourceRequest("CSO", "CSO-H-1", new BigDecimal("10"), "manual attach")
        ));

        settlementService.postDetailsFromDocuments("SET-1", request);
        settlementService.postDetailsFromDocuments("SET-1", request);

        ArgumentCaptor<SettlementDetailEntity> detailCaptor = ArgumentCaptor.forClass(SettlementDetailEntity.class);
        verify(settlementRequestMapper).insertDetail(detailCaptor.capture());
        SettlementDetailEntity inserted = detailCaptor.getValue();
        assertEquals("CSO", inserted.getDocumentType());
        assertEquals("CSO-00001", inserted.getDocumentNo());
        assertEquals(new BigDecimal("3"), inserted.getQty());
        assertEquals(new BigDecimal("10"), inserted.getUnitPrice());
        assertEquals(new BigDecimal("30"), inserted.getLineAmount());
    }

    @Test
    void shouldResolveUnitPriceFromPricingSourceWhenRequestPriceIsMissing() {
        SettlementRequestEntity settlement = settlementHeader("SET-1", "CUSTOMER", "HELD");
        CsoHeaderEntity csoHeader = new CsoHeaderEntity();
        csoHeader.setId("CSO-H-1");
        csoHeader.setDocNo("CSO-00001");
        csoHeader.setCompany("COMP01");
        csoHeader.setStore("STORE01");
        csoHeader.setCustomerCode("CUST01");
        csoHeader.setSupplierCode("SUPP01");
        csoHeader.setSupplierContract("CONTRACT01");
        csoHeader.setStatus("RELEASED");

        CsoDetailEntity csoDetail = new CsoDetailEntity();
        csoDetail.setItemCode("SKU-1");
        csoDetail.setQty(new BigDecimal("4"));
        csoDetail.setUom("PCS");

        when(settlementRequestMapper.findHeaderById("SET-1")).thenReturn(settlement);
        when(csoMapper.findHeaderById("CSO-H-1")).thenReturn(csoHeader);
        when(csoMapper.findDetailsByHeaderId("CSO-H-1")).thenReturn(List.of(csoDetail));
        when(settlementRequestMapper.countExistingDetail("SET-1", "CSO", "CSO-00001", "SKU-1")).thenReturn(0);
        when(settlementRequestMapper.findDetailsByHeaderId("SET-1")).thenReturn(List.of());
        when(pricingService.resolveUnitPrice("SKU-1", "COMP01", "STORE01", "SUPP01", "CONTRACT01", "CUST01"))
                .thenReturn(new BigDecimal("7.25"));

        SettlementDocumentPostRequest request = new SettlementDocumentPostRequest(List.of(
                new SettlementDocumentSourceRequest("CSO", "CSO-H-1", null, "auto pricing")
        ));

        settlementService.postDetailsFromDocuments("SET-1", request);

        ArgumentCaptor<SettlementDetailEntity> detailCaptor = ArgumentCaptor.forClass(SettlementDetailEntity.class);
        verify(settlementRequestMapper, times(1)).insertDetail(detailCaptor.capture());
        SettlementDetailEntity inserted = detailCaptor.getValue();
        assertEquals(new BigDecimal("7.25"), inserted.getUnitPrice());
        assertEquals(new BigDecimal("29.00"), inserted.getLineAmount());
        verify(pricingService, times(1)).resolveUnitPrice("SKU-1", "COMP01", "STORE01", "SUPP01", "CONTRACT01", "CUST01");
    }

    @Test
    void shouldRejectBatchGenerationWhenOpenReferenceAlreadyExists() {
        when(settlementRequestMapper.findHeaderById(anyString())).thenReturn(settlementHeader("SET-1", "SUPPLIER", "HELD"));
        when(settlementRequestMapper.findDetailsByHeaderId(anyString())).thenReturn(List.of());
        when(settlementRequestMapper.countOpenByReference("COMP01", "STORE01", "SUPPLIER", "WEEKLY-TEST")).thenReturn(2);

        SettlementBatchGenerateRequest request = new SettlementBatchGenerateRequest(
                "COMP01",
                "STORE01",
                "SUPPLIER",
                null,
                "SUPP01",
                "CONTRACT01",
                "IDR",
                "tester",
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 3, 31),
                BigDecimal.ONE,
                "WEEKLY-TEST"
        );

        assertThrows(BusinessRuleViolationException.class, () -> settlementService.generateBatch(request));
    }

    @Test
    void shouldPersistComputedTotalAmountWhenPreparingForBilling() {
        SettlementRequestEntity header = settlementHeader("SET-1", "SUPPLIER", "HELD");
        SettlementDetailEntity detailOne = new SettlementDetailEntity();
        detailOne.setLineAmount(new BigDecimal("15.50"));
        SettlementDetailEntity detailTwo = new SettlementDetailEntity();
        detailTwo.setLineAmount(new BigDecimal("4.50"));

        when(settlementRequestMapper.findHeaderById("SET-1")).thenReturn(header);
        when(settlementRequestMapper.findDetailsByHeaderId("SET-1")).thenReturn(List.of(detailOne, detailTwo));

        settlementService.prepareForBilling("SET-1");

        verify(settlementRequestMapper).updateTotalAmount("SET-1", new BigDecimal("20.00"));
        verify(settlementRequestMapper).updateHeaderStatus(eq("SET-1"), eq("READY_FOR_BILLING"), any(), eq(null), eq(null));
    }

    @Test
    void shouldRejectBatchGenerationWhenFromDateAfterToDate() {
        SettlementBatchGenerateRequest request = new SettlementBatchGenerateRequest(
                "COMP01",
                "STORE01",
                "SUPPLIER",
                null,
                "SUPP01",
                "CONTRACT01",
                "IDR",
                "tester",
                LocalDate.of(2026, 3, 31),
                LocalDate.of(2026, 3, 1),
                BigDecimal.ONE,
                "WEEKLY-TEST"
        );

        assertThrows(BusinessRuleViolationException.class, () -> settlementService.generateBatch(request));
    }

    private SettlementRequestEntity settlementHeader(String id, String type, String status) {
        SettlementRequestEntity header = new SettlementRequestEntity();
        header.setId(id);
        header.setDocNo("SETTL-00001");
        header.setCompany("COMP01");
        header.setStore("STORE01");
        header.setSettlementType(type);
        header.setCustomerCode("CUST01");
        header.setSupplierCode("SUPP01");
        header.setSupplierContract("CONTRACT01");
        header.setCurrency("IDR");
        header.setCreatedBy("tester");
        header.setStatus(status);
        return header;
    }
}