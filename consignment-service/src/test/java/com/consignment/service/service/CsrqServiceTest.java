package com.consignment.service.service;

import com.consignment.service.exception.BusinessRuleViolationException;
import com.consignment.service.model.csrq.CsrqDetailRequest;
import com.consignment.service.model.csrq.CsrqRequest;
import com.consignment.service.persistence.mapper.CsrqMapper;
import com.consignment.service.persistence.model.CsrqHeaderEntity;
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
class CsrqServiceTest {

    @Mock
    private CsrqMapper csrqMapper;

    @Mock
    private NotificationService notificationService;

    private CsrqService csrqService;

    @BeforeEach
    void setUp() {
        csrqService = new CsrqService(csrqMapper, notificationService);
    }

    @Test
    void shouldRejectCreateWhenItemNotInSetup() {
        CsrqRequest request = new CsrqRequest(
                "COMP01",
                "STORE01",
                "SUP01",
                "CTR01",
                null,
                null,
                null,
                "user1",
                "MANUAL",
                null,
                List.of(new CsrqDetailRequest("SKU01", BigDecimal.ONE, "PCS"))
        );

        when(csrqMapper.countMatchingSetup("SKU01", "SUP01", "CTR01", "STORE01", null)).thenReturn(0L);

        assertThrows(BusinessRuleViolationException.class, () -> csrqService.create(request));
        verify(csrqMapper, never()).insertHeader(any());
    }

    @Test
    void shouldRejectReleaseWhenStatusIsNotHeld() {
        CsrqHeaderEntity header = new CsrqHeaderEntity();
        header.setId("1");
        header.setStatus("RELEASED");

        when(csrqMapper.findHeaderById("1")).thenReturn(header);

        assertThrows(BusinessRuleViolationException.class, () -> csrqService.release("1"));
        verify(notificationService, never()).sendCsrqReleased(any(), any());
    }

    @Test
    void shouldRejectDeleteWhenStatusIsNotHeld() {
        CsrqHeaderEntity header = new CsrqHeaderEntity();
        header.setId("1");
        header.setStatus("RELEASED");

        when(csrqMapper.findHeaderById("1")).thenReturn(header);

        assertThrows(BusinessRuleViolationException.class, () -> csrqService.delete("1"));
    }
}
