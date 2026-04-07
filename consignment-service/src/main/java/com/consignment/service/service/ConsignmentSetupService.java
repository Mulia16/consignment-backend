package com.consignment.service.service;

import com.consignment.service.exception.BusinessRuleViolationException;
import com.consignment.service.exception.ResourceNotFoundException;
import com.consignment.service.model.PageMeta;
import com.consignment.service.model.setup.*;
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

    public ConsignmentSetupService(ItemSetupMapper itemSetupMapper,
                                   ConsignmentSetupMapper consignmentSetupMapper,
                                   InventoryValidationMapper inventoryValidationMapper) {
        this.itemSetupMapper = itemSetupMapper;
        this.consignmentSetupMapper = consignmentSetupMapper;
        this.inventoryValidationMapper = inventoryValidationMapper;
    }

    // ── List with filter + pagination ──────────────────────────────────────────

    public record PagedItems(List<ConsignmentSetupItemResponse> items, PageMeta meta) {}

    public PagedItems listItems(ItemSetupSearchCriteria criteria) {
        List<ItemSetupEntity> entities = consignmentSetupMapper.searchItems(criteria);
        long total = consignmentSetupMapper.countItems(criteria);
        List<ConsignmentSetupItemResponse> items = entities.stream()
                .map(e -> toResponse(e,
                        consignmentSetupMapper.findExternalByItemCode(e.getItemCode()),
                        consignmentSetupMapper.findInternalByItemCode(e.getItemCode())))
                .toList();
        return new PagedItems(items, PageMeta.of(criteria.page(), criteria.perPage(), total));
    }

    // ── Single item ────────────────────────────────────────────────────────────

    public ConsignmentSetupItemResponse getByItemCode(String itemCode) {
        ItemSetupEntity itemSetup = itemSetupMapper.findByItemCode(itemCode);
        List<ExternalSupplierEntity> ext = consignmentSetupMapper.findExternalByItemCode(itemCode);
        List<InternalSupplierEntity> intl = consignmentSetupMapper.findInternalByItemCode(itemCode);
        if (itemSetup == null && ext.isEmpty() && intl.isEmpty()) {
            throw new ResourceNotFoundException("Consignment setup not found for item: " + itemCode);
        }
        return toResponse(itemSetup, ext, intl);
    }

    // ── Create / Update item setup ─────────────────────────────────────────────

    @Transactional
    public ConsignmentSetupItemResponse createOrUpdateItem(ItemSetupRequest request) {
        ItemSetupEntity entity = new ItemSetupEntity();
        entity.setItemCode(request.itemCode());
        entity.setItemName(request.itemName());
        entity.setVariant(request.variant());
        entity.setHierarchy(request.hierarchy());
        entity.setItemModel(request.itemModel());
        entity.setUnitRetail(request.unitRetail());
        entity.setMvc(request.mvc());
        entity.setCategoryL1(request.categoryL1());
        entity.setCategoryL2(request.categoryL2());
        entity.setCategoryL3(request.categoryL3());
        entity.setSyncFlag(true);
        entity.setDeletedFlag(false);
        itemSetupMapper.upsert(entity);
        return getByItemCode(request.itemCode());
    }

    // ── External supplier ──────────────────────────────────────────────────────

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
        return toExternalResponse(consignmentSetupMapper.findExternalById(itemCode, entity.getId()));
    }

    @Transactional
    public ExternalSupplierSetupResponse updateExternalSupplier(String itemCode, String id, ExternalSupplierSetupRequest request) {
        ensureItemExists(itemCode);
        ExternalSupplierEntity existing = consignmentSetupMapper.findExternalById(itemCode, id);
        if (existing == null) throw new ResourceNotFoundException("External supplier setup not found: " + id);
        if (inventoryValidationMapper.countBlockingInventory(itemCode, existing.getConsigneeStore()) > 0) {
            throw new BusinessRuleViolationException("Cannot update while related inventory is not zero");
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
        if (existing == null) throw new ResourceNotFoundException("External supplier setup not found: " + id);
        if (inventoryValidationMapper.countBlockingInventory(itemCode, existing.getConsigneeStore()) > 0) {
            throw new BusinessRuleViolationException("Cannot delete while related inventory is not zero");
        }
        consignmentSetupMapper.deleteExternal(itemCode, id);
    }

    // ── Internal supplier ──────────────────────────────────────────────────────

    @Transactional
    public InternalSupplierSetupResponse addInternalSupplier(String itemCode, InternalSupplierSetupRequest request) {
        ensureItemExists(itemCode);
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
                .filter(i -> entity.getId().equals(i.getId()))
                .findFirst().map(this::toInternalResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Internal supplier not found after insert"));
    }

    // ── Guards ─────────────────────────────────────────────────────────────────

    private void ensureItemExists(String itemCode) {
        itemSetupMapper.ensureExists(itemCode, DEFAULT_HIERARCHY);
    }

    private void ensureItemAllowsExternal(String itemCode) {
        ItemSetupEntity e = itemSetupMapper.findByItemCode(itemCode);
        if (e != null && OUTRIGHT_HIERARCHY.equalsIgnoreCase(e.getHierarchy())) {
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

    private void ensureOneStoreOneSupplier(String itemCode, String storeCode, String supplierCode, String ignoreId) {
        // untuk external: cek consigneeStore tidak dipakai supplier berbeda
        consignmentSetupMapper.findExternalByItemCode(itemCode).forEach(e -> {
            if (!e.getId().equals(ignoreId) && e.getConsigneeStore().equals(storeCode) && !e.getSupplierCode().equals(supplierCode)) {
                throw new BusinessRuleViolationException("A store can only map to one supplier per item");
            }
        });
        // untuk internal: cek supplierStore tidak dipakai supplier berbeda
        consignmentSetupMapper.findInternalByItemCode(itemCode).forEach(i -> {
            if (i.getSupplierStore().equals(storeCode) && !i.getSupplierCode().equals(supplierCode)) {
                throw new BusinessRuleViolationException("A store can only map to one supplier per item");
            }
        });
    }

    private void ensureNotUsedByInternal(String itemCode, String store) {
        if (consignmentSetupMapper.findInternalByItemCode(itemCode).stream().anyMatch(i -> i.getSupplierStore().equals(store))) {
            throw new BusinessRuleViolationException("Store already used in internal setup for this item");
        }
    }

    private void ensureNotUsedByExternal(String itemCode, String store) {
        if (consignmentSetupMapper.findExternalByItemCode(itemCode).stream().anyMatch(e -> e.getConsigneeStore().equals(store))) {
            throw new BusinessRuleViolationException("Store already used in external setup for this item");
        }
    }

    private void ensureInternalHierarchy(String itemCode, String supplierStore) {
        if (consignmentSetupMapper.findExternalByItemCode(itemCode).stream().noneMatch(e -> e.getConsigneeStore().equals(supplierStore))) {
            throw new BusinessRuleViolationException("Internal supplier store must belong to external consignee hierarchy");
        }
    }

    // ── Mappers ────────────────────────────────────────────────────────────────

    private ConsignmentSetupItemResponse toResponse(ItemSetupEntity item,
                                                     List<ExternalSupplierEntity> ext,
                                                     List<InternalSupplierEntity> intl) {
        ConsignmentSetupItemResponse.CategoryHierarchy category = null;
        if (item != null && (item.getCategoryL1() != null || item.getCategoryL2() != null || item.getCategoryL3() != null)) {
            category = new ConsignmentSetupItemResponse.CategoryHierarchy(item.getCategoryL1(), item.getCategoryL2(), item.getCategoryL3());
        }
        String itemCode = item != null ? item.getItemCode() : (ext.isEmpty() ? "" : ext.get(0).getItemCode());
        return new ConsignmentSetupItemResponse(
                itemCode,
                item != null ? item.getItemName() : null,
                item != null ? item.getVariant() : null,
                item != null ? item.getHierarchy() : null,
                item != null ? item.getItemModel() : null,
                item != null ? item.getUnitRetail() : null,
                item != null ? item.getMvc() : null,
                category,
                ext.stream().map(this::toExternalResponse).toList(),
                intl.stream().map(this::toInternalResponse).toList()
        );
    }

    private ExternalSupplierSetupResponse toExternalResponse(ExternalSupplierEntity e) {
        return new ExternalSupplierSetupResponse(e.getId(), e.getSupplierCode(), e.getSupplierType(),
                e.getSupplierContract(), e.getConsigneeCompany(), e.getConsigneeStore(),
                e.getCurrentInventoryQty(), e.getCreatedAt(), e.getUpdatedAt());
    }

    private InternalSupplierSetupResponse toInternalResponse(InternalSupplierEntity e) {
        return new InternalSupplierSetupResponse(e.getId(), e.getSupplierCode(), e.getSupplierStore(),
                e.getConsigneeCompany(), e.getConsigneeStore(), e.getCreatedAt());
    }
}
