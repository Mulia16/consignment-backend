package com.consignment.service.service;

import com.consignment.service.exception.BusinessRuleViolationException;
import com.consignment.service.model.csrn.CsrnActualQtyUpdateRequest;
import com.consignment.service.persistence.mapper.CsrnCMapper;
import com.consignment.service.persistence.mapper.CsrnMapper;
import com.consignment.service.persistence.mapper.InventoryMutationMapper;
import com.consignment.service.persistence.model.CsrnHeaderEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CsrServiceTest {

    @Mock
    private CsrnMapper csrnMapper;

    @Mock
    private CsrnCMapper csrnCMapper;

    @Mock
    private InventoryMutationMapper inventoryMutationMapper;

    @Mock
    private NotificationService notificationService;

    private CsrnService csrnService;

    @BeforeEach
    void setUp() {
        csrnService = new CsrnService(csrnMapper, csrnCMapper, inventoryMutationMapper, notificationService);
    }

    @Test
    void shouldRejectActualQtyUpdateWhenNotReleased() {
        CsrnHeaderEntity header = new CsrnHeaderEntity();
        header.setId("CSRN-1");
        header.setStatus("HELD");

        when(csrnMapper.findHeaderById("CSRN-1")).thenReturn(header);

        assertThrows(
                BusinessRuleViolationException.class,
                () -> csrnService.updateActualQty("CSRN-1", "DETAIL-1", new CsrnActualQtyUpdateRequest(BigDecimal.ONE))
        );
    }

    @Test
    void shouldRejectCompleteWhenNotReleased() {
        CsrnHeaderEntity header = new CsrnHeaderEntity();
        header.setId("CSRN-1");
        header.setStatus("HELD");

        when(csrnMapper.findHeaderById("CSRN-1")).thenReturn(header);

        assertThrows(BusinessRuleViolationException.class, () -> csrnService.complete("CSRN-1"));
    }
}
