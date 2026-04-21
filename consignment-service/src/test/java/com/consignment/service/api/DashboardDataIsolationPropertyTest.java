package com.consignment.service.api;

// Feature: consignee-role, Property 14: Dashboard Data Isolation

import com.consignment.service.config.ConsigneeContext;
import com.consignment.service.model.ApiResponse;
import com.consignment.service.model.consignee.ConsigneeDashboardResponse;
import com.consignment.service.persistence.mapper.ConsigneePurchaseOrderMapper;
import com.consignment.service.persistence.mapper.ConsignmentSetupMapper;
import com.consignment.service.persistence.mapper.CsoMapper;
import com.consignment.service.persistence.model.ExternalSupplierEntity;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Property-based tests for Dashboard Data Isolation.
 *
 * Validates: Requirements 11.1, 11.4, 12.1, 12.2
 */
class DashboardDataIsolationPropertyTest {

    /**
     * Property 14: Dashboard Data Isolation
     *
     * For any consignee user with store value S, GET /api/consignee/dashboard SHALL return only
     * items where store == S. No item with a different store value SHALL appear in the response.
     *
     * Validates: Requirements 11.1, 11.4, 12.1, 12.2
     */
    @Property(tries = 100)
    void dashboardDataIsolation(
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

        // Seed matching inventory items for the store
        List<ExternalSupplierEntity> matchingItems = buildEntities(store, matchingCount, "ITEM-MATCH-");
        when(consignmentSetupMapper.findByConsigneeStore(store)).thenReturn(matchingItems);

        // Seed sales data for the store
        List<Map<String, Object>> salesRows = buildSalesRows(matchingItems);
        when(csoMapper.findSalesQtyByStore(store)).thenReturn(salesRows);

        ConsigneeController controller = new ConsigneeController(
                consigneeContext, consignmentSetupMapper, poMapper, csoMapper);

        // Act
        ResponseEntity<ApiResponse<List<ConsigneeDashboardResponse>>> response = controller.getDashboard();

        // Assert: response is 200 OK
        assertThat(response.getStatusCode().value()).isEqualTo(200);

        List<ConsigneeDashboardResponse> dashboard = response.getBody().data();
        assertThat(dashboard).isNotNull();

        // Assert: every item in the response has store == jwt_store
        for (ConsigneeDashboardResponse item : dashboard) {
            assertThat(item.store())
                    .as("Expected store to be '%s' but was '%s'", store, item.store())
                    .isEqualTo(store);
        }

        // Assert: no items from otherStore appear in the response
        long leakedItems = dashboard.stream()
                .filter(d -> otherStore.equals(d.store()))
                .count();
        assertThat(leakedItems)
                .as("No items from store '%s' should appear in response for store '%s'", otherStore, store)
                .isEqualTo(0L);

        // Assert: the mappers were called with the correct store filter
        verify(consignmentSetupMapper).findByConsigneeStore(store);
        verify(csoMapper).findSalesQtyByStore(store);
        verify(consignmentSetupMapper, never()).findByConsigneeStore(otherStore);
        verify(csoMapper, never()).findSalesQtyByStore(otherStore);
    }

    /**
     * Property 14 (empty case): When no items are assigned to the store,
     * the dashboard response SHALL be an empty list (not null, not 404).
     *
     * Validates: Requirements 11.1, 11.5
     */
    @Property(tries = 50)
    void dashboardDataIsolationEmptyStore(@ForAll("storeValues") String store) {
        ConsigneeContext consigneeContext = mock(ConsigneeContext.class);
        ConsignmentSetupMapper consignmentSetupMapper = mock(ConsignmentSetupMapper.class);
        ConsigneePurchaseOrderMapper poMapper = mock(ConsigneePurchaseOrderMapper.class);
        CsoMapper csoMapper = mock(CsoMapper.class);

        when(consigneeContext.requireStore()).thenReturn(store);
        when(consignmentSetupMapper.findByConsigneeStore(store)).thenReturn(List.of());
        when(csoMapper.findSalesQtyByStore(store)).thenReturn(List.of());

        ConsigneeController controller = new ConsigneeController(
                consigneeContext, consignmentSetupMapper, poMapper, csoMapper);

        ResponseEntity<ApiResponse<List<ConsigneeDashboardResponse>>> response = controller.getDashboard();

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

    private List<ExternalSupplierEntity> buildEntities(String consigneeStore, int count, String prefix) {
        List<ExternalSupplierEntity> entities = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ExternalSupplierEntity e = new ExternalSupplierEntity();
            e.setItemCode(prefix + i);
            e.setItemName("Item " + i);
            e.setConsigneeStore(consigneeStore);
            e.setCurrentInventoryQty(10 + i);
            entities.add(e);
        }
        return entities;
    }

    private List<Map<String, Object>> buildSalesRows(List<ExternalSupplierEntity> items) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (ExternalSupplierEntity item : items) {
            rows.add(Map.of("item_code", item.getItemCode(), "sales_qty", 5));
        }
        return rows;
    }
}
