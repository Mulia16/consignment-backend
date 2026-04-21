package com.consignment.service.api;

// Feature: consignee-role, Property 10: Products Data Isolation

import com.consignment.service.config.ConsigneeContext;
import com.consignment.service.model.ApiResponse;
import com.consignment.service.model.consignee.ConsigneeProductResponse;
import com.consignment.service.persistence.mapper.ConsigneePurchaseOrderMapper;
import com.consignment.service.persistence.mapper.ConsignmentSetupMapper;
import com.consignment.service.persistence.mapper.CsoMapper;
import com.consignment.service.persistence.model.ExternalSupplierEntity;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Property-based tests for Products Data Isolation.
 *
 * Validates: Requirements 7.1, 7.2, 12.1, 12.2
 */
class ProductsDataIsolationPropertyTest {

    /**
     * Property 10: Products Data Isolation
     *
     * For any consignee user with store value S, GET /api/consignee/products SHALL return only
     * items where consigneeStore == S. No item with a different consigneeStore value SHALL appear
     * in the response.
     *
     * Validates: Requirements 7.1, 7.2, 12.1, 12.2
     */
    @Property(tries = 100)
    void productsDataIsolation(
            @ForAll("storeValues") String store,
            @ForAll("otherStoreValues") String otherStore,
            @ForAll @IntRange(min = 0, max = 10) int matchingCount,
            @ForAll @IntRange(min = 1, max = 10) int otherCount
    ) {
        // Arrange
        ConsigneeContext consigneeContext = mock(ConsigneeContext.class);
        ConsignmentSetupMapper mapper = mock(ConsignmentSetupMapper.class);
        ConsigneePurchaseOrderMapper poMapper = mock(ConsigneePurchaseOrderMapper.class);
        CsoMapper csoMapper = mock(CsoMapper.class);

        when(consigneeContext.requireStore()).thenReturn(store);

        // Build matching entities (consigneeStore == store)
        List<ExternalSupplierEntity> matchingEntities = buildEntities(store, matchingCount, "ITEM-MATCH-");
        when(mapper.findByConsigneeStore(store)).thenReturn(matchingEntities);

        ConsigneeController controller = new ConsigneeController(consigneeContext, mapper, poMapper, csoMapper);

        // Act
        ResponseEntity<ApiResponse<List<ConsigneeProductResponse>>> response = controller.getProducts();

        // Assert: response is 200 OK
        assertThat(response.getStatusCode().value()).isEqualTo(200);

        List<ConsigneeProductResponse> products = response.getBody().data();
        assertThat(products).isNotNull();

        // Assert: every item in the response has consigneeStore == store
        for (ConsigneeProductResponse product : products) {
            assertThat(product.consigneeStore())
                    .as("Expected consigneeStore to be '%s' but was '%s'", store, product.consigneeStore())
                    .isEqualTo(store);
        }

        // Assert: no items from otherStore appear in the response
        long leakedItems = products.stream()
                .filter(p -> otherStore.equals(p.consigneeStore()))
                .count();
        assertThat(leakedItems)
                .as("No items from store '%s' should appear in response for store '%s'", otherStore, store)
                .isEqualTo(0L);

        // Assert: the mapper was called with the correct store filter
        verify(mapper).findByConsigneeStore(store);
        verify(mapper, never()).findByConsigneeStore(otherStore);
    }

    /**
     * Property 10 (empty case): When no items are assigned to the store,
     * the response SHALL be an empty list (not null, not 404).
     *
     * Validates: Requirements 7.1, 7.3
     */
    @Property(tries = 50)
    void productsDataIsolationEmptyStore(@ForAll("storeValues") String store) {
        ConsigneeContext consigneeContext = mock(ConsigneeContext.class);
        ConsignmentSetupMapper mapper = mock(ConsignmentSetupMapper.class);
        ConsigneePurchaseOrderMapper poMapper = mock(ConsigneePurchaseOrderMapper.class);
        CsoMapper csoMapper = mock(CsoMapper.class);

        when(consigneeContext.requireStore()).thenReturn(store);
        when(mapper.findByConsigneeStore(store)).thenReturn(List.of());

        ConsigneeController controller = new ConsigneeController(consigneeContext, mapper, poMapper, csoMapper);

        ResponseEntity<ApiResponse<List<ConsigneeProductResponse>>> response = controller.getProducts();

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
            e.setVariant("VAR-" + i);
            e.setSupplierCode("SUP-" + i);
            e.setSupplierContract("CONTRACT-" + i);
            e.setConsigneeStore(consigneeStore);
            entities.add(e);
        }
        return entities;
    }
}
