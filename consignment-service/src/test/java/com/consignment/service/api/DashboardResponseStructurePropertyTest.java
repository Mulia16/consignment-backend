package com.consignment.service.api;

// Feature: consignee-role, Property 16: Dashboard Response Structure

import com.consignment.service.config.ConsigneeContext;
import com.consignment.service.model.ApiResponse;
import com.consignment.service.model.consignee.ConsigneeDashboardResponse;
import com.consignment.service.persistence.mapper.ConsigneePurchaseOrderMapper;
import com.consignment.service.persistence.mapper.ConsignmentSetupMapper;
import com.consignment.service.persistence.mapper.CsoMapper;
import com.consignment.service.persistence.model.ExternalSupplierEntity;
import net.jqwik.api.*;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Property-based tests for Dashboard Response Structure.
 *
 * Validates: Requirements 11.6
 */
class DashboardResponseStructurePropertyTest {

    /**
     * Property 16: Dashboard Response Structure
     *
     * For any item returned by GET /api/consignee/dashboard, the response object SHALL contain
     * all required fields: itemCode, itemName, salesQty, currentStockQty, store.
     *
     * Validates: Requirements 11.6
     */
    @Property(tries = 100)
    void dashboardResponseStructure(
            @ForAll("storeValues") String store,
            @ForAll("dashboardEntities") List<ExternalSupplierEntity> entities
    ) {
        // Arrange
        ConsigneeContext consigneeContext = mock(ConsigneeContext.class);
        ConsignmentSetupMapper consignmentSetupMapper = mock(ConsignmentSetupMapper.class);
        ConsigneePurchaseOrderMapper poMapper = mock(ConsigneePurchaseOrderMapper.class);
        CsoMapper csoMapper = mock(CsoMapper.class);

        when(consigneeContext.requireStore()).thenReturn(store);

        // Assign the fixed store to all entities
        entities.forEach(e -> e.setConsigneeStore(store));
        when(consignmentSetupMapper.findByConsigneeStore(store)).thenReturn(entities);

        // Build sales rows for each entity
        List<Map<String, Object>> salesRows = entities.stream()
                .map(e -> Map.<String, Object>of("item_code", e.getItemCode(), "sales_qty", 5))
                .collect(Collectors.toList());
        when(csoMapper.findSalesQtyByStore(store)).thenReturn(salesRows);

        ConsigneeController controller = new ConsigneeController(
                consigneeContext, consignmentSetupMapper, poMapper, csoMapper);

        // Act
        ResponseEntity<ApiResponse<List<ConsigneeDashboardResponse>>> response = controller.getDashboard();

        // Assert: response is 200 OK
        assertThat(response.getStatusCode().value()).isEqualTo(200);

        List<ConsigneeDashboardResponse> dashboard = response.getBody().data();
        assertThat(dashboard).isNotNull();

        // Assert: every item contains all required fields with non-null values
        for (ConsigneeDashboardResponse item : dashboard) {
            assertThat(item.itemCode())
                    .as("itemCode must not be null")
                    .isNotNull();
            assertThat(item.itemName())
                    .as("itemName must not be null")
                    .isNotNull();
            assertThat(item.salesQty())
                    .as("salesQty must not be null")
                    .isNotNull();
            assertThat(item.currentStockQty())
                    .as("currentStockQty must not be null")
                    .isNotNull();
            assertThat(item.store())
                    .as("store must not be null")
                    .isNotNull();
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

    @Provide
    Arbitrary<List<ExternalSupplierEntity>> dashboardEntities() {
        Arbitrary<ExternalSupplierEntity> entityArbitrary = Combinators.combine(
                Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(20),
                Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(50),
                Arbitraries.integers().between(0, 500)
        ).as((itemCode, itemName, currentInventoryQty) -> {
            ExternalSupplierEntity e = new ExternalSupplierEntity();
            e.setItemCode(itemCode);
            e.setItemName(itemName);
            e.setCurrentInventoryQty(currentInventoryQty);
            return e;
        });
        return entityArbitrary.list().ofMinSize(1).ofMaxSize(15);
    }
}
