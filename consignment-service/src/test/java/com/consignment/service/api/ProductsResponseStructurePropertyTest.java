package com.consignment.service.api;

// Feature: consignee-role, Property 11: Products Response Structure

import com.consignment.service.config.ConsigneeContext;
import com.consignment.service.model.ApiResponse;
import com.consignment.service.model.consignee.ConsigneeProductResponse;
import com.consignment.service.persistence.mapper.ConsigneePurchaseOrderMapper;
import com.consignment.service.persistence.mapper.ConsignmentSetupMapper;
import com.consignment.service.persistence.mapper.CsoMapper;
import com.consignment.service.persistence.model.ExternalSupplierEntity;
import net.jqwik.api.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Property-based tests for Products Response Structure.
 *
 * Validates: Requirements 7.4
 */
class ProductsResponseStructurePropertyTest {

    /**
     * Property 11: Products Response Structure
     *
     * For any item returned by GET /api/consignee/products, the response object SHALL contain
     * all required fields: itemCode, itemName, variant, supplierCode, supplierContract,
     * consigneeStore.
     *
     * Validates: Requirements 7.4
     */
    @Property(tries = 100)
    void productsResponseStructure(
            @ForAll("storeValues") String store,
            @ForAll("productEntities") List<ExternalSupplierEntity> entities
    ) {
        // Arrange
        ConsigneeContext consigneeContext = mock(ConsigneeContext.class);
        ConsignmentSetupMapper mapper = mock(ConsignmentSetupMapper.class);
        ConsigneePurchaseOrderMapper poMapper = mock(ConsigneePurchaseOrderMapper.class);
        CsoMapper csoMapper = mock(CsoMapper.class);

        when(consigneeContext.requireStore()).thenReturn(store);

        // Assign the fixed store to all entities
        entities.forEach(e -> e.setConsigneeStore(store));
        when(mapper.findByConsigneeStore(store)).thenReturn(entities);

        ConsigneeController controller = new ConsigneeController(consigneeContext, mapper, poMapper, csoMapper);

        // Act
        ResponseEntity<ApiResponse<List<ConsigneeProductResponse>>> response = controller.getProducts();

        // Assert: response is 200 OK
        assertThat(response.getStatusCode().value()).isEqualTo(200);

        List<ConsigneeProductResponse> products = response.getBody().data();
        assertThat(products).isNotNull();

        // Assert: every item contains all required fields with non-null values
        for (ConsigneeProductResponse product : products) {
            assertThat(product.itemCode())
                    .as("itemCode must not be null")
                    .isNotNull();
            assertThat(product.itemName())
                    .as("itemName must not be null")
                    .isNotNull();
            assertThat(product.variant())
                    .as("variant must not be null")
                    .isNotNull();
            assertThat(product.supplierCode())
                    .as("supplierCode must not be null")
                    .isNotNull();
            assertThat(product.supplierContract())
                    .as("supplierContract must not be null")
                    .isNotNull();
            assertThat(product.consigneeStore())
                    .as("consigneeStore must not be null")
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
    Arbitrary<List<ExternalSupplierEntity>> productEntities() {
        Arbitrary<ExternalSupplierEntity> entityArbitrary = Combinators.combine(
                Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(20),
                Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(50),
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(20),
                Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(20),
                Arbitraries.strings().alpha().ofMinLength(3).ofMaxLength(30)
        ).as((itemCode, itemName, variant, supplierCode, supplierContract) -> {
            ExternalSupplierEntity e = new ExternalSupplierEntity();
            e.setItemCode(itemCode);
            e.setItemName(itemName);
            e.setVariant(variant);
            e.setSupplierCode(supplierCode);
            e.setSupplierContract(supplierContract);
            return e;
        });
        return entityArbitrary.list().ofMinSize(1).ofMaxSize(15);
    }
}
