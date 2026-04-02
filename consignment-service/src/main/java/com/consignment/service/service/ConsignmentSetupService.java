package com.consignment.service.service;

import com.consignment.service.exception.BusinessRuleViolationException;
import com.consignment.service.exception.ResourceNotFoundException;
import com.consignment.service.model.setup.ConsignmentSetupItemResponse;
import com.consignment.service.model.setup.ExternalSupplierSetupRequest;
import com.consignment.service.model.setup.ExternalSupplierSetupResponse;
import com.consignment.service.model.setup.InternalSupplierSetupRequest;
import com.consignment.service.model.setup.InternalSupplierSetupResponse;
import com.consignment.service.persistence.mapper.ConsignmentSetupMapper;
import com.consignment.service.persistence.mapper.InventoryValidationMapper;
import com.consignment.service.persistence.mapper.ItemSetupMapper;
import com.consignment.service.persistence.model.ExternalSupplierEntity;
import com.consignment.service.persistence.model.InternalSupplierEntity;
import com.consignment.service.persistence.model.ItemSetupEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ConsignmentSetupService {

    private static final String DEFAULT_HIERARCHY = "CONSIGNMENT";
    private static final String OUTRIGHT_HIERARCHY = "OUTRIGHT";

    private final ItemSetupMapper itemSetupMapper;
    private final ConsignmentSetupMapper consignmentSetupMapper;
    private final InventoryValidationMapper inventoryValidationMapper;

    public ConsignmentSetupService(
            ItemSetupMapper itemSetupMapper,
            ConsignmentSetupMapper consignmentSetupMapper,
            InventoryValidationMapper inventoryValidationMapper
    ) {
        this.itemSetupMapper = itemSetupMapper;
        this.consignmentSetupMapper = consignmentSetupMapper;
        this.inventoryValidationMapper = inventoryValidationMapper;
    }

    public List<ConsignmentSetupItemResponse> listItems() {
        return consignmentSetupMapper.findItemCodesWithSetup().stream()
                .map(this::getByItemCode)
                .toList();
    }

    public ConsignmentSetupItemResponse getByItemCode(String itemCode) {
        ItemSetupEntity itemSetup = itemSetupMapper.findByItemCode(itemCode);
        List<ExternalSupplierEntity> externalSuppliers = consignmentSetupMapper.findExternalByItemCode(itemCode);
        List<InternalSupplierEntity> internalSuppliers = consignmentSetupMapper.findInternalByItemCode(itemCode);

        if (itemSetup == null && externalSuppliers.isEmpty() && internalSuppliers.isEmpty()) {
            throw new ResourceNotFoundException("Consignment setup not found for item: " + itemCode);
        }

        return toResponse(itemCode, externalSuppliers, internalSuppliers);
    }

    @Transactional
    public ExternalSupplierSetupResponse addExternalSupplier(String itemCode, ExternalSupplierSetupRequest request) {
        ensureItemExists(itemCode);
        ensureExternalSupplierType(request.supplierType());
        ensureItemAllowsExternal(itemCode);
        ensureOneStoreOneSupplier(itemCode, request.consigneeStore(), request.supplierCode());
        ensureNotUsedByInternal(itemCode, request.consigneeStore());

        ExternalSupplierEntity entity = new ExternalSupplierEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setItemCode(itemCode);
        entity.setSupplierCode(request.supplierCode());
        entity.setSupplierType(request.supplierType());
        entity.setSupplierContract(request.contractNumber());
        entity.setConsigneeCompany(request.consigneeCompany());
        entity.setConsigneeStore(request.consigneeStore());
        entity.setCurrentInventoryQty(request.currentInventoryQty());
        consignmentSetupMapper.insertExternal(entity);

        ExternalSupplierEntity saved = consignmentSetupMapper.findExternalById(itemCode, entity.getId());
        return toExternalResponse(saved);
    }

    @Transactional
    public ExternalSupplierSetupResponse updateExternalSupplier(
            String itemCode,
            String id,
            ExternalSupplierSetupRequest request
    ) {
        ensureItemExists(itemCode);
        ExternalSupplierEntity existing = consignmentSetupMapper.findExternalById(itemCode, id);
        if (existing == null) {
            throw new ResourceNotFoundException("External supplier setup not found: " + id);
        }
        if (inventoryValidationMapper.countBlockingInventory(itemCode, existing.getConsigneeStore()) > 0) {
            throw new BusinessRuleViolationException("External supplier setup cannot be updated while related inventory is not zero");
        }

        ensureExternalSupplierType(request.supplierType());
        ensureItemAllowsExternal(itemCode);
        ensureOneStoreOneSupplier(itemCode, request.consigneeStore(), request.supplierCode(), id);
        ensureNotUsedByInternal(itemCode, request.consigneeStore());

        existing.setSupplierCode(request.supplierCode());
        existing.setSupplierType(request.supplierType());
        existing.setSupplierContract(request.contractNumber());
        existing.setConsigneeCompany(request.consigneeCompany());
        existing.setConsigneeStore(request.consigneeStore());
        existing.setCurrentInventoryQty(request.currentInventoryQty());

        consignmentSetupMapper.updateExternal(existing);
        return toExternalResponse(consignmentSetupMapper.findExternalById(itemCode, id));
    }

    @Transactional
    public void deleteExternalSupplier(String itemCode, String id) {
        ExternalSupplierEntity existing = consignmentSetupMapper.findExternalById(itemCode, id);
        if (existing == null) {
            throw new ResourceNotFoundException("External supplier setup not found: " + id);
        }
        if (inventoryValidationMapper.countBlockingInventory(itemCode, existing.getConsigneeStore()) > 0) {
            throw new BusinessRuleViolationException("External supplier setup cannot be deleted while related inventory is not zero");
        }
        consignmentSetupMapper.deleteExternal(itemCode, id);
    }

    @Transactional
    public InternalSupplierSetupResponse addInternalSupplier(String itemCode, InternalSupplierSetupRequest request) {
        ensureItemExists(itemCode);
        ensureOneStoreOneSupplier(itemCode, request.supplierStore(), request.supplierCode());
        ensureNotUsedByExternal(itemCode, request.supplierStore());
        ensureInternalHierarchy(itemCode, request.supplierStore());

        InternalSupplierEntity entity = new InternalSupplierEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setItemCode(itemCode);
        entity.setSupplierCode(request.supplierCode());
        entity.setSupplierStore(request.supplierStore());
        entity.setConsigneeCompany(request.consigneeCompany());
        entity.setConsigneeStore(request.consigneeStore());
        consignmentSetupMapper.insertInternal(entity);

        return consignmentSetupMapper.findInternalByItemCode(itemCode).stream()
                .filter(internal -> entity.getId().equals(internal.getId()))
                .findFirst()
                .map(this::toInternalResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Internal supplier setup not found after insert: " + entity.getId()));
    }

    private void ensureItemExists(String itemCode) {
        itemSetupMapper.ensureExists(itemCode, DEFAULT_HIERARCHY);
    }

    private void ensureItemAllowsExternal(String itemCode) {
        ItemSetupEntity itemSetupEntity = itemSetupMapper.findByItemCode(itemCode);
        if (itemSetupEntity != null && OUTRIGHT_HIERARCHY.equalsIgnoreCase(itemSetupEntity.getHierarchy())) {
            throw new BusinessRuleViolationException("Outright items cannot be assigned to external consignment supplier");
        }
    }

    private void ensureExternalSupplierType(String supplierType) {
        if (!"EXTERNAL".equalsIgnoreCase(supplierType)) {
            throw new BusinessRuleViolationException("External setup requires supplier type EXTERNAL");
        }
    }

    private void ensureOneStoreOneSupplier(String itemCode, String storeCode, String supplierCode) {
        ensureOneStoreOneSupplier(itemCode, storeCode, supplierCode, null);
    }

    private void ensureOneStoreOneSupplier(String itemCode, String storeCode, String supplierCode, String externalIdToIgnore) {
        consignmentSetupMapper.findExternalByItemCode(itemCode).forEach(external -> {
            if (!external.getId().equals(externalIdToIgnore)
                    && external.getConsigneeStore().equals(storeCode)
                    && !external.getSupplierCode().equals(supplierCode)) {
                throw new BusinessRuleViolationException("A store can only map to one supplier per item");
            }
        });

        consignmentSetupMapper.findInternalByItemCode(itemCode).forEach(internal -> {
            if (internal.getSupplierStore().equals(storeCode) && !internal.getSupplierCode().equals(supplierCode)) {
                throw new BusinessRuleViolationException("A store can only map to one supplier per item");
            }
        });
    }

    private void ensureNotUsedByInternal(String itemCode, String externalStore) {
        boolean alreadyUsed = consignmentSetupMapper.findInternalByItemCode(itemCode).stream()
                .anyMatch(internal -> internal.getSupplierStore().equals(externalStore));
        if (alreadyUsed) {
            throw new BusinessRuleViolationException(
                    "Store already used in internal setup for this item"
            );
        }
    }

    private void ensureNotUsedByExternal(String itemCode, String internalStore) {
        boolean alreadyUsed = consignmentSetupMapper.findExternalByItemCode(itemCode).stream()
                .anyMatch(external -> external.getConsigneeStore().equals(internalStore));
        if (alreadyUsed) {
            throw new BusinessRuleViolationException(
                    "Store already used in external setup for this item"
            );
        }
    }

    private void ensureInternalHierarchy(String itemCode, String internalSupplierStore) {
        boolean existsInExternalHierarchy = consignmentSetupMapper.findExternalByItemCode(itemCode).stream()
                .anyMatch(external -> external.getConsigneeStore().equals(internalSupplierStore));

        if (!existsInExternalHierarchy) {
            throw new BusinessRuleViolationException(
                    "Internal supplier store must belong to external consignee hierarchy"
            );
        }
    }

    private ConsignmentSetupItemResponse toResponse(
            String itemCode,
            List<ExternalSupplierEntity> externalSuppliers,
            List<InternalSupplierEntity> internalSuppliers
    ) {
        return new ConsignmentSetupItemResponse(
                itemCode,
                externalSuppliers.stream().map(this::toExternalResponse).toList(),
                internalSuppliers.stream().map(this::toInternalResponse).toList()
        );
    }

    private ExternalSupplierSetupResponse toExternalResponse(ExternalSupplierEntity entry) {
        return new ExternalSupplierSetupResponse(
                entry.getId(),
                entry.getSupplierCode(),
                entry.getSupplierType(),
                entry.getSupplierContract(),
                entry.getConsigneeCompany(),
                entry.getConsigneeStore(),
                entry.getCurrentInventoryQty(),
                entry.getCreatedAt(),
                entry.getUpdatedAt()
        );
    }

    private InternalSupplierSetupResponse toInternalResponse(InternalSupplierEntity entry) {
        return new InternalSupplierSetupResponse(
                entry.getId(),
                entry.getSupplierCode(),
                entry.getSupplierStore(),
                entry.getConsigneeCompany(),
                entry.getConsigneeStore(),
                entry.getCreatedAt()
        );
    }
}
