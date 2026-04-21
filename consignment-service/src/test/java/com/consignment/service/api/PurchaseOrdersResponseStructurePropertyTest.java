package com.consignment.service.api;

// Feature: consignee-role, Property 13: Purchase Orders Response Structure

import com.consignment.service.config.ConsigneeContext;
import com.consignment.service.model.ApiResponse;
import com.consignment.service.model.consignee.ConsigneePurchaseOrderResponse;
import com.consignment.service.persistence.mapper.ConsigneePurchaseOrderMapper;
import com.consignment.service.persistence.mapper.ConsignmentSetupMapper;
import com.consignment.service.persistence.mapper.CsoMapper;
import com.consignment.service.persistence.model.ConsigneePurchaseOrderEntity;
import net.jqwik.api.*;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Property-based tests for Purchase Orders Response Structure.
 *
 * Validates: Requirements 8.4
 */
class PurchaseOrdersResponseStructurePropertyTest {

    /**
     * Property 13: Purchase Orders Response Structure
     *
     * For any PO returned by GET /api/consignee/purchase-orders, the response object SHALL contain
     * all required fields: poNumber, store, itemCode, itemName, orderedQty, status, poDate.
     *
     * Validates: Requirements 8.4
     */
    @Property(tries = 100)
    void purchaseOrdersResponseStructure(
            @ForAll("storeValues") String store,
            @ForAll("purchaseOrderEntities") List<ConsigneePurchaseOrderEntity> entities
    ) {
        // Arrange
        ConsigneeContext consigneeContext = mock(ConsigneeContext.class);
        ConsignmentSetupMapper consignmentSetupMapper = mock(ConsignmentSetupMapper.class);
        ConsigneePurchaseOrderMapper poMapper = mock(ConsigneePurchaseOrderMapper.class);
        CsoMapper csoMapper = mock(CsoMapper.class);

        when(consigneeContext.requireStore()).thenReturn(store);

        // Assign the fixed store to all entities
        entities.forEach(e -> e.setStore(store));
        when(poMapper.findByStore(store)).thenReturn(entities);

        ConsigneeController controller = new ConsigneeController(consigneeContext, consignmentSetupMapper, poMapper, csoMapper);

        // Act
        ResponseEntity<ApiResponse<List<ConsigneePurchaseOrderResponse>>> response = controller.getPurchaseOrders();

        // Assert: response is 200 OK
        assertThat(response.getStatusCode().value()).isEqualTo(200);

        List<ConsigneePurchaseOrderResponse> purchaseOrders = response.getBody().data();
        assertThat(purchaseOrders).isNotNull();

        // Assert: every PO contains all required fields with non-null values
        for (ConsigneePurchaseOrderResponse po : purchaseOrders) {
            assertThat(po.poNumber())
                    .as("poNumber must not be null")
                    .isNotNull();
            assertThat(po.store())
                    .as("store must not be null")
                    .isNotNull();
            assertThat(po.itemCode())
                    .as("itemCode must not be null")
                    .isNotNull();
            assertThat(po.itemName())
                    .as("itemName must not be null")
                    .isNotNull();
            assertThat(po.orderedQty())
                    .as("orderedQty must not be null")
                    .isNotNull();
            assertThat(po.status())
                    .as("status must not be null")
                    .isNotNull();
            assertThat(po.poDate())
                    .as("poDate must not be null")
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
    Arbitrary<List<ConsigneePurchaseOrderEntity>> purchaseOrderEntities() {
        Arbitrary<ConsigneePurchaseOrderEntity> entityArbitrary = Combinators.combine(
                Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(20),
                Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(50),
                Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(50),
                Arbitraries.integers().between(1, 9999),
                Arbitraries.of("PENDING", "APPROVED", "RECEIVED", "CANCELLED"),
                Arbitraries.integers().between(2020, 2025),
                Arbitraries.integers().between(1, 12),
                Arbitraries.integers().between(1, 28)
        ).as((poNumber, itemCode, itemName, orderedQty, status, year, month, day) -> {
            ConsigneePurchaseOrderEntity e = new ConsigneePurchaseOrderEntity();
            e.setPoNumber(poNumber);
            e.setItemCode(itemCode);
            e.setItemName(itemName);
            e.setOrderedQty(orderedQty);
            e.setStatus(status);
            e.setPoDate(LocalDate.of(year, month, day));
            return e;
        });
        return entityArbitrary.list().ofMinSize(1).ofMaxSize(15);
    }
}
