package com.consignment.service.service;

import com.consignment.service.exception.BusinessRuleViolationException;
import com.consignment.service.model.csdo.CsdoTransferRequest;
import com.consignment.service.persistence.mapper.CsoMapper;
import com.consignment.service.persistence.mapper.CsdoMapper;
import com.consignment.service.persistence.mapper.InventoryMutationMapper;
import com.consignment.service.persistence.mapper.ReservationMapper;
import com.consignment.service.persistence.model.CsoHeaderEntity;
import com.consignment.service.persistence.model.CsdoHeaderEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CsdoServiceTest {

    @Mock
    private CsdoMapper csdoMapper;

    @Mock
    private CsoMapper csoMapper;

    @Mock
    private ReservationMapper reservationMapper;

    @Mock
    private InventoryMutationMapper inventoryMutationMapper;

    private CsdoService csdoService;

    @BeforeEach
    void setUp() {
        csdoService = new CsdoService(csdoMapper, csoMapper, reservationMapper, inventoryMutationMapper);
    }

    @Test
    void shouldRejectTransferWhenCsoIsNotReleased() {
        CsoHeaderEntity cso = new CsoHeaderEntity();
        cso.setId("CSO-1");
        cso.setStatus("HELD");

        when(csoMapper.findHeaderById("CSO-1")).thenReturn(cso);

        assertThrows(
                BusinessRuleViolationException.class,
                () -> csdoService.transferFromCso("CSO-1", new CsdoTransferRequest(true, null, null, "user1"))
        );
    }

    @Test
    void shouldRejectReleaseWhenCsdoIsNotHeld() {
        CsdoHeaderEntity csdo = new CsdoHeaderEntity();
        csdo.setId("CSDO-1");
        csdo.setStatus("RELEASED");

        when(csdoMapper.findHeaderById("CSDO-1")).thenReturn(csdo);

        assertThrows(BusinessRuleViolationException.class, () -> csdoService.release("CSDO-1"));
    }
}
