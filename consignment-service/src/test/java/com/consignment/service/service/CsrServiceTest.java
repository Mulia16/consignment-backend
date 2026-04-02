package com.consignment.service.service;

import com.consignment.service.exception.BusinessRuleViolationException;
import com.consignment.service.model.csr.CsrActualQtyUpdateRequest;
import com.consignment.service.persistence.mapper.CsrMapper;
import com.consignment.service.persistence.mapper.InventoryMutationMapper;
import com.consignment.service.persistence.model.CsrHeaderEntity;
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
    private CsrMapper csrMapper;

    @Mock
    private InventoryMutationMapper inventoryMutationMapper;

    @Mock
    private NotificationService notificationService;

    private CsrService csrService;

    @BeforeEach
    void setUp() {
        csrService = new CsrService(csrMapper, inventoryMutationMapper, notificationService);
    }

    @Test
    void shouldRejectActualQtyUpdateWhenNotReleased() {
        CsrHeaderEntity header = new CsrHeaderEntity();
        header.setId("CSR-1");
        header.setStatus("HELD");

        when(csrMapper.findHeaderById("CSR-1")).thenReturn(header);

        assertThrows(
                BusinessRuleViolationException.class,
                () -> csrService.updateActualQty("CSR-1", "DETAIL-1", new CsrActualQtyUpdateRequest(BigDecimal.ONE))
        );
    }

    @Test
    void shouldRejectCompleteWhenNotReleased() {
        CsrHeaderEntity header = new CsrHeaderEntity();
        header.setId("CSR-1");
        header.setStatus("HELD");

        when(csrMapper.findHeaderById("CSR-1")).thenReturn(header);

        assertThrows(BusinessRuleViolationException.class, () -> csrService.complete("CSR-1"));
    }
}
