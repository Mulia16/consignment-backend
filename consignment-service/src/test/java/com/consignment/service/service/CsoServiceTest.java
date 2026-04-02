package com.consignment.service.service;

import com.consignment.service.exception.BusinessRuleViolationException;
import com.consignment.service.model.cso.CsoDetailRequest;
import com.consignment.service.model.cso.CsoRequest;
import com.consignment.service.persistence.mapper.CsoMapper;
import com.consignment.service.persistence.mapper.ReservationMapper;
import com.consignment.service.persistence.model.CsoHeaderEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CsoServiceTest {

    @Mock
    private CsoMapper csoMapper;

    @Mock
    private ReservationMapper reservationMapper;

    @Mock
    private CsdoService csdoService;

    private CsoService csoService;

    @BeforeEach
    void setUp() {
        csoService = new CsoService(csoMapper, reservationMapper, csdoService);
    }

    @Test
    void shouldRejectAutoCreateWhenMethodIsNotApi() {
        CsoRequest request = new CsoRequest(
                "COMP01",
                "STORE01",
                "CUST01",
                "BR01",
                "cust@example.com",
                "SUP01",
                "CTR01",
                false,
                null,
                "system",
                "MANUAL",
                "REF01",
                List.of(new CsoDetailRequest("SKU01", BigDecimal.ONE, "PCS"))
        );

        assertThrows(BusinessRuleViolationException.class, () -> csoService.autoCreate(request));
    }

    @Test
    void shouldRejectCreateWhenSetupDoesNotMatch() {
        CsoRequest request = new CsoRequest(
                "COMP01",
                "STORE01",
                "CUST01",
                "BR01",
                "cust@example.com",
                "SUP01",
                "CTR01",
                false,
                null,
                "user1",
                "MANUAL",
                "REF01",
                List.of(new CsoDetailRequest("SKU01", BigDecimal.ONE, "PCS"))
        );

        when(csoMapper.countMatchingSetup("SKU01", "SUP01", "CTR01", "STORE01")).thenReturn(0L);

        assertThrows(BusinessRuleViolationException.class, () -> csoService.create(request));
        verify(csoMapper, never()).insertHeader(any());
    }

    @Test
    void shouldRejectReleaseWhenStatusIsNotHeld() {
        CsoHeaderEntity header = new CsoHeaderEntity();
        header.setId("1");
        header.setStatus("RELEASED");

        when(csoMapper.findHeaderById("1")).thenReturn(header);

        assertThrows(BusinessRuleViolationException.class, () -> csoService.release("1", "tester"));
        verify(reservationMapper, never()).insertReservation(any(), any(), any(), any(), any(), any());
    }
}
