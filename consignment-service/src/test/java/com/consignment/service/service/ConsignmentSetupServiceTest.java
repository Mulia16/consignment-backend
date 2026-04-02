package com.consignment.service.service;

import com.consignment.service.exception.BusinessRuleViolationException;
import com.consignment.service.model.setup.ExternalSupplierSetupRequest;
import com.consignment.service.model.setup.InternalSupplierSetupRequest;
import com.consignment.service.persistence.mapper.ConsignmentSetupMapper;
import com.consignment.service.persistence.mapper.InventoryValidationMapper;
import com.consignment.service.persistence.mapper.ItemSetupMapper;
import com.consignment.service.persistence.model.ExternalSupplierEntity;
import com.consignment.service.persistence.model.ItemSetupEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsignmentSetupServiceTest {

    @Mock
    private ItemSetupMapper itemSetupMapper;

    @Mock
    private ConsignmentSetupMapper consignmentSetupMapper;

    @Mock
    private InventoryValidationMapper inventoryValidationMapper;

    private ConsignmentSetupService consignmentSetupService;

    @BeforeEach
    void setUp() {
        consignmentSetupService = new ConsignmentSetupService(
                itemSetupMapper,
                consignmentSetupMapper,
                inventoryValidationMapper
        );
    }

    @Test
    void shouldRejectExternalSupplierForOutrightItem() {
        ItemSetupEntity itemSetupEntity = new ItemSetupEntity();
        itemSetupEntity.setItemCode("SKU01");
        itemSetupEntity.setHierarchy("OUTRIGHT");

        doNothing().when(itemSetupMapper).ensureExists("SKU01", "CONSIGNMENT");
        when(itemSetupMapper.findByItemCode("SKU01")).thenReturn(itemSetupEntity);

        ExternalSupplierSetupRequest request = new ExternalSupplierSetupRequest(
                "SUP-01",
                "EXTERNAL",
                "CTR-01",
                "COMP-01",
                "STORE-01",
                0
        );

        assertThrows(
                BusinessRuleViolationException.class,
                () -> consignmentSetupService.addExternalSupplier("SKU01", request)
        );
    }

    @Test
    void shouldRejectExternalSupplierUpdateWhenInventoryExists() {
        ExternalSupplierEntity existing = new ExternalSupplierEntity();
        existing.setId("EXT-1");
        existing.setItemCode("SKU01");
        existing.setConsigneeStore("STORE-01");

        doNothing().when(itemSetupMapper).ensureExists("SKU01", "CONSIGNMENT");
        when(consignmentSetupMapper.findExternalById("SKU01", "EXT-1")).thenReturn(existing);
        when(inventoryValidationMapper.countBlockingInventory("SKU01", "STORE-01")).thenReturn(1L);

        ExternalSupplierSetupRequest request = new ExternalSupplierSetupRequest(
                "SUP-01",
                "EXTERNAL",
                "CTR-01",
                "COMP-01",
                "STORE-01",
                0
        );

        assertThrows(
                BusinessRuleViolationException.class,
                () -> consignmentSetupService.updateExternalSupplier("SKU01", "EXT-1", request)
        );
    }

    @Test
    void shouldRejectInternalSupplierOutsideExternalHierarchy() {
        doNothing().when(itemSetupMapper).ensureExists("SKU01", "CONSIGNMENT");
        when(consignmentSetupMapper.findExternalByItemCode("SKU01")).thenReturn(List.of());
        when(consignmentSetupMapper.findInternalByItemCode("SKU01")).thenReturn(List.of());

        InternalSupplierSetupRequest request = new InternalSupplierSetupRequest(
                "SUP-INT-01",
                "STORE-INT-01",
                "COMP-01",
                "STORE-01"
        );

        assertThrows(
                BusinessRuleViolationException.class,
                () -> consignmentSetupService.addInternalSupplier("SKU01", request)
        );
    }
}
