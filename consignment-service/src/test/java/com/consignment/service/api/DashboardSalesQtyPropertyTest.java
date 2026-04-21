package com.consignment.service.api;

// Feature: consignee-role, Property 15: Dashboard salesQty Computation Consistency

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
 * Property-based tests for Dashboard salesQty Computation Consistency.
 *
 * Validates: Requirements 11.2
 */
class DashboardSalesQtyPropertyTest {

    /**
     * Property 15: Dashboard salesQty Computation Consistency
     *
     * For any store S, the salesQty for each item in the dashboard response SHALL equal the
     * sum of quantities from all CSO/CSDO transaction records for that item filtered by store == S.
     *
     * Validates: Requirements 11.2
     */
    @Property(tries = 100)
    void dashboardSalesQtyComputationConsistency(
            @ForAll("storeValues") String store,
            @ForAll @IntRange(min = 1, max = 10) int itemCount
    ) {
        // Arrange
        ConsigneeContext consigneeContext = mock(ConsigneeContext.class);
        ConsignmentSetupMapper consignmentSetupMapper = mock(ConsignmentSetupMapper.class);
        ConsigneePurchaseOrderMapper poMapper = mock(ConsigneePurchaseOrderMapper.class);
        CsoMapper csoMapper = mock(CsoMapper.class);

        when(consigneeContext.requireStore()).thenReturn(store);

        // Build items and their expected sales quantities
        List<ExternalSupplierEntity> items = buildItems(store, itemCount);
        List<Map<String, Object>> salesRows = buildSalesRows(items);

        when(consignmentSetupMapper.findByConsigneeStore(store)).thenReturn(items);
        when(csoMapper.findSalesQtyByStore(store)).thenReturn(salesRows);

        ConsigneeController controller = new ConsigneeController(
                consigneeContext, consignmentSetupMapper, poMapper, csoMapper);

        // Act
        ResponseEntity<ApiResponse<List<ConsigneeDashboardResponse>>> response = controller.getDashboard();

        // Assert: response is 200 OK
        assertThat(response.getStatusCode().value()).isEqualTo(200);

        List<ConsigneeDashboardResponse> dashboard = response.getBody().data();
        assertThat(dashboard).isNotNull().hasSize(itemCount);

        // Build expected salesQty map from the mock sales rows
        Map<String, Integer> expectedSalesQty = new java.util.HashMap<>();
        for (Map<String, Object> row : salesRows) {
            String itemCode = (String) row.get("item_code");
            Number qty = (Number) row.get("sales_qty");
            expectedSalesQty.put(itemCode, qty != null ? qty.intValue() : 0);
        }

        // Assert: each item's salesQty equals the corresponding sales_qty from mock data
        for (ConsigneeDashboardResponse item : dashboard) {
            int expected = expectedSalesQty.getOrDefault(item.itemCode(), 0);
            assertThat(item.salesQty())
                    .as("salesQty for item '%s' should be %d but was %d",
                            item.itemCode(), expected, item.salesQty())
                    .isEqualTo(expected);
        }
    }

    /**
     * Property 15 (zero sales case): When no CSO/CSDO transactions exist for the store,
     * all items in the dashboard SHALL have salesQty == 0.
     *
     * Validates: Requirements 11.2
     */
    @Property(tries = 50)
    void dashboardSalesQtyIsZeroWhenNoTransactions(
            @ForAll("storeValues") String store,
            @ForAll @IntRange(min = 1, max = 10) int itemCount
    ) {
        ConsigneeContext consigneeContext = mock(ConsigneeContext.class);
        ConsignmentSetupMapper consignmentSetupMapper = mock(ConsignmentSetupMapper.class);
        ConsigneePurchaseOrderMapper poMapper = mock(ConsigneePurchaseOrderMapper.class);
        CsoMapper csoMapper = mock(CsoMapper.class);

        when(consigneeContext.requireStore()).thenReturn(store);

        List<ExternalSupplierEntity> items = buildItems(store, itemCount);
        when(consignmentSetupMapper.findByConsigneeStore(store)).thenReturn(items);
        when(csoMapper.findSalesQtyByStore(store)).thenReturn(List.of());

        ConsigneeController controller = new ConsigneeController(
                consigneeContext, consignmentSetupMapper, poMapper, csoMapper);

        ResponseEntity<ApiResponse<List<ConsigneeDashboardResponse>>> response = controller.getDashboard();

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        List<ConsigneeDashboardResponse> dashboard = response.getBody().data();
        assertThat(dashboard).isNotNull().hasSize(itemCount);

        for (ConsigneeDashboardResponse item : dashboard) {
            assertThat(item.salesQty())
                    .as("salesQty for item '%s' should be 0 when no transactions exist", item.itemCode())
                    .isEqualTo(0);
        }
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

    // ── Helpers ──────────────────────────────────────────────────────────────

    private List<ExternalSupplierEntity> buildItems(String store, int count) {
        List<ExternalSupplierEntity> items = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ExternalSupplierEntity e = new ExternalSupplierEntity();
            e.setItemCode("ITEM-" + i);
            e.setItemName("Item " + i);
            e.setConsigneeStore(store);
            e.setCurrentInventoryQty(10 + i);
            items.add(e);
        }
        return items;
    }

    private List<Map<String, Object>> buildSalesRows(List<ExternalSupplierEntity> items) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            ExternalSupplierEntity item = items.get(i);
            // Use a deterministic but varied sales qty per item
            int salesQty = (i + 1) * 3;
            rows.add(Map.of("item_code", item.getItemCode(), "sales_qty", salesQty));
        }
        return rows;
    }
}
