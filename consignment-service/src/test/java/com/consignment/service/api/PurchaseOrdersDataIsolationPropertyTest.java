package com.consignment.service.api;

// Feature: consignee-role, Property 12: Purchase Orders Data Isolation

import com.consignment.service.config.ConsigneeContext;
import com.consignment.service.model.ApiResponse;
import com.consignment.service.model.consignee.ConsigneePurchaseOrderResponse;
import com.consignment.service.persistence.mapper.ConsigneePurchaseOrderMapper;
import com.consignment.service.persistence.mapper.ConsignmentSetupMapper;
import com.consignment.service.persistence.mapper.CsoMapper;
import com.consignment.service.persistence.model.ConsigneePurchaseOrderEntity;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Property-based tests for Purchase Orders Data Isolation.
 *
 * Validates: Requirements 8.1, 8.2
 */
class PurchaseOrdersDataIsolationPropertyTest {

    /**
     * Property 12: Purchase Orders Data Isolation
     *
     * For any consignee user with store value S, GET /api/consignee/purchase-orders SHALL return
     * only POs where store == S. No PO with a different store value SHALL appear in the response.
     *
     * Validates: Requirements 8.1, 8.2
     */
    @Property(tries = 100)
    void purchaseOrdersDataIsolation(
            @ForAll("storeValues") String store,
            @ForAll("otherStoreValues") String otherStore,
            @ForAll @IntRange(min = 0, max = 10) int matchingCount,
            @ForAll @IntRange(min = 1, max = 10) int otherCount
    ) {
        // Arrange
        ConsigneeContext consigneeContext = mock(ConsigneeContext.class);
        ConsignmentSetupMapper consignmentSetupMapper = mock(ConsignmentSetupMapper.class);
        ConsigneePurchaseOrderMapper poMapper = mock(ConsigneePurchaseOrderMapper.class);
        CsoMapper csoMapper = mock(CsoMapper.class);

        when(consigneeContext.requireStore()).thenReturn(store);

        // Build matching PO entities (store == store)
        List<ConsigneePurchaseOrderEntity> matchingEntities = buildPoEntities(store, matchingCount, "PO-MATCH-");
        when(poMapper.findByStore(store)).thenReturn(matchingEntities);

        ConsigneeController controller = new ConsigneeController(consigneeContext, consignmentSetupMapper, poMapper, csoMapper);

        // Act
        ResponseEntity<ApiResponse<List<ConsigneePurchaseOrderResponse>>> response = controller.getPurchaseOrders();

        // Assert: response is 200 OK
        assertThat(response.getStatusCode().value()).isEqualTo(200);

        List<ConsigneePurchaseOrderResponse> purchaseOrders = response.getBody().data();
        assertThat(purchaseOrders).isNotNull();

        // Assert: every PO in the response has store == store
        for (ConsigneePurchaseOrderResponse po : purchaseOrders) {
            assertThat(po.store())
                    .as("Expected store to be '%s' but was '%s'", store, po.store())
                    .isEqualTo(store);
        }

        // Assert: no POs from otherStore appear in the response
        long leakedPOs = purchaseOrders.stream()
                .filter(po -> otherStore.equals(po.store()))
                .count();
        assertThat(leakedPOs)
                .as("No POs from store '%s' should appear in response for store '%s'", otherStore, store)
                .isEqualTo(0L);

        // Assert: the mapper was called with the correct store filter
        verify(poMapper).findByStore(store);
        verify(poMapper, never()).findByStore(otherStore);
    }

    /**
     * Property 12 (empty case): When no POs exist for the store,
     * the response SHALL be an empty list (not null, not 404).
     *
     * Validates: Requirements 8.1, 8.3
     */
    @Property(tries = 50)
    void purchaseOrdersDataIsolationEmptyStore(@ForAll("storeValues") String store) {
        ConsigneeContext consigneeContext = mock(ConsigneeContext.class);
        ConsignmentSetupMapper consignmentSetupMapper = mock(ConsignmentSetupMapper.class);
        ConsigneePurchaseOrderMapper poMapper = mock(ConsigneePurchaseOrderMapper.class);
        CsoMapper csoMapper = mock(CsoMapper.class);

        when(consigneeContext.requireStore()).thenReturn(store);
        when(poMapper.findByStore(store)).thenReturn(List.of());

        ConsigneeController controller = new ConsigneeController(consigneeContext, consignmentSetupMapper, poMapper, csoMapper);

        ResponseEntity<ApiResponse<List<ConsigneePurchaseOrderResponse>>> response = controller.getPurchaseOrders();

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().data()).isNotNull().isEmpty();
    }

    // ── Arbitraries ──────────────────────────────────────────────────────────

    @Provide
    Arbitrary<String> storeValues() {
        return Arbitraries.strings()
                .withCharRange('A', 'Z')
                .withCharRange('0', '9')
                .ofMinLength(3)
                .ofMaxLength(20)
                .map(s -> "STORE_" + s);
    }

    @Provide
    Arbitrary<String> otherStoreValues() {
        return Arbitraries.strings()
                .withCharRange('A', 'Z')
                .withCharRange('0', '9')
                .ofMinLength(3)
                .ofMaxLength(20)
                .map(s -> "OTHER_" + s);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private List<ConsigneePurchaseOrderEntity> buildPoEntities(String store, int count, String prefix) {
        List<ConsigneePurchaseOrderEntity> entities = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ConsigneePurchaseOrderEntity e = new ConsigneePurchaseOrderEntity();
            e.setPoNumber(prefix + i);
            e.setStore(store);
            e.setItemCode("ITEM-" + i);
            e.setItemName("Item " + i);
            e.setOrderedQty(10 + i);
            e.setStatus("PENDING");
            e.setPoDate(LocalDate.of(2024, 1, 1));
            entities.add(e);
        }
        return entities;
    }
}
