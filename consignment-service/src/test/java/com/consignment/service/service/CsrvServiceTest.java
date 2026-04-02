package com.consignment.service.service;

import com.consignment.service.exception.BusinessRuleViolationException;
import com.consignment.service.model.csrv.CsrvDetailRequest;
import com.consignment.service.model.csrv.CsrvRequest;
import com.consignment.service.persistence.mapper.CsrvMapper;
import com.consignment.service.persistence.mapper.SupplierBookValueInventoryMapper;
import com.consignment.service.persistence.model.CsrvHeaderEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CsrvServiceTest {

    @Mock
    private CsrvMapper csrvMapper;

    @Mock
    private SupplierBookValueInventoryMapper supplierBookValueInventoryMapper;

    private CsrvService csrvService;

    @BeforeEach
    void setUp() {
        csrvService = new CsrvService(csrvMapper, supplierBookValueInventoryMapper);
    }

    @Test
    void shouldRejectAutoCreateWhenMethodIsNotApi() {
        CsrvRequest request = new CsrvRequest(
                "COMP01",
                "STORE01",
                "SUP01",
                "CTR01",
                null,
                "DO01",
                LocalDate.now(),
                null,
                "system",
                "MANUAL",
                "REF01",
                List.of(new CsrvDetailRequest("SKU01", BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ONE))
        );

        assertThrows(BusinessRuleViolationException.class, () -> csrvService.autoCreate(request));
    }

    @Test
    void shouldRejectCreateWhenItemNotInSetup() {
        CsrvRequest request = new CsrvRequest(
                "COMP01",
                "STORE01",
                "SUP01",
                "CTR01",
                null,
                "DO01",
                LocalDate.now(),
                null,
                "user1",
                "MANUAL",
                "REF01",
                List.of(new CsrvDetailRequest("SKU01", BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ONE))
        );

        when(csrvMapper.countMatchingSetup("SKU01", "SUP01", "CTR01", "STORE01")).thenReturn(0L);

        assertThrows(BusinessRuleViolationException.class, () -> csrvService.create(request));
        verify(csrvMapper, never()).insertHeader(any());
    }

    @Test
    void shouldRejectReleaseWhenStatusIsNotHeld() {
        CsrvHeaderEntity header = new CsrvHeaderEntity();
        header.setId("1");
        header.setStatus("RELEASED");

        when(csrvMapper.findHeaderById("1")).thenReturn(header);

        assertThrows(BusinessRuleViolationException.class, () -> csrvService.release("1"));
        verify(supplierBookValueInventoryMapper, never()).upsertReceiving(any(), any(), any(), any(), any());
    }
}
