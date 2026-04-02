package com.consignment.service.service;

import com.consignment.service.exception.BusinessRuleViolationException;
import com.consignment.service.model.csa.CsaDetailRequest;
import com.consignment.service.model.csa.CsaRequest;
import com.consignment.service.persistence.mapper.CsaMapper;
import com.consignment.service.persistence.mapper.InventoryMutationMapper;
import com.consignment.service.persistence.model.CsaHeaderEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CsaServiceTest {

    @Mock
    private CsaMapper csaMapper;

    @Mock
    private InventoryMutationMapper inventoryMutationMapper;

    private CsaService csaService;

    @BeforeEach
    void setUp() {
        csaService = new CsaService(csaMapper, inventoryMutationMapper);
    }

    @Test
    void shouldRejectInvalidTransactionTypeOnCreate() {
        CsaRequest request = new CsaRequest(
                "COMP01",
                "STORE01",
                "SUP01",
                "CTR01",
                "INVALID",
                null,
                null,
                null,
                null,
                "user1",
                List.of(new CsaDetailRequest("SKU01", "Item", BigDecimal.ONE, "PCS", "UNPOST_SALES"))
        );

        assertThrows(BusinessRuleViolationException.class, () -> csaService.create(request));
    }

    @Test
    void shouldRejectReleaseWhenNotHeld() {
        CsaHeaderEntity header = new CsaHeaderEntity();
        header.setId("CSA-1");
        header.setStatus("RELEASED");

        when(csaMapper.findHeaderById("CSA-1")).thenReturn(header);

        assertThrows(BusinessRuleViolationException.class, () -> csaService.release("CSA-1", "user1"));
    }
}
