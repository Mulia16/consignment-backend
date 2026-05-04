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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ConsignmentSetupService {

    private static final Logger log = LoggerFactory.getLogger(ConsignmentSetupService.class);

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
        log.debug("Listing setup items page={} perPage={} itemCode={}", criteria.page(), criteria.perPage(), criteria.itemCode());
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
        log.debug("Fetching setup by itemCode={}", itemCode);
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
        log.info("Upserting item setup itemCode={} hierarchy={}", request.itemCode(), request.hierarchy());
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
        log.info("Adding external supplier itemCode={} supplierCode={} contract={}", itemCode, request.supplierCode(), request.contractNumber());
        ensureItemExists(itemCode);
        ensureExternalSupplierType(request.supplierType());
        ensureItemAllowsExternal(itemCode);

        for (ExternalSupplierSetupRequest.ConsigneeRequest consignee : request.consignees()) {
            for (String storeId : consignee.storeIds()) {
                ensureNotUsedByInternal(itemCode, storeId);
                ExternalSupplierEntity entity = new ExternalSupplierEntity();
                entity.setId(UUID.randomUUID().toString());
                entity.setItemCode(itemCode);
                entity.setSupplierCode(request.supplierCode());
                entity.setSupplierType(request.supplierType());
                entity.setSupplierContract(request.contractNumber());
                entity.setConsigneeCompany(consignee.companyId());
                entity.setConsigneeStore(storeId);
                entity.setCurrentInventoryQty(0);
                consignmentSetupMapper.insertExternal(entity);
            }
        }
        return toExternalGroupedResponse(request.supplierCode(), request.supplierType(), request.contractNumber(),
                consignmentSetupMapper.findExternalByItemCodeAndSupplier(itemCode, request.supplierCode(), request.contractNumber()));
    }

    @Transactional
    public ExternalSupplierSetupResponse updateExternalSupplier(String itemCode, String supplierCode, String contractNumber,
                                                                 ExternalSupplierSetupRequest request) {
        log.info("Updating external supplier itemCode={} supplierCode={} contract={}", itemCode, supplierCode, contractNumber);
        ensureItemExists(itemCode);
        ensureExternalSupplierType(request.supplierType());
        ensureItemAllowsExternal(itemCode);

        // delete existing rows for this supplier+contract, then re-insert
        consignmentSetupMapper.deleteExternalBySupplier(itemCode, supplierCode, contractNumber);

        for (ExternalSupplierSetupRequest.ConsigneeRequest consignee : request.consignees()) {
            for (String storeId : consignee.storeIds()) {
                ensureNotUsedByInternal(itemCode, storeId);
                ExternalSupplierEntity entity = new ExternalSupplierEntity();
                entity.setId(UUID.randomUUID().toString());
                entity.setItemCode(itemCode);
                entity.setSupplierCode(request.supplierCode());
                entity.setSupplierType(request.supplierType());
                entity.setSupplierContract(request.contractNumber());
                entity.setConsigneeCompany(consignee.companyId());
                entity.setConsigneeStore(storeId);
                entity.setCurrentInventoryQty(0);
                consignmentSetupMapper.insertExternal(entity);
            }
        }
        return toExternalGroupedResponse(request.supplierCode(), request.supplierType(), request.contractNumber(),
                consignmentSetupMapper.findExternalByItemCodeAndSupplier(itemCode, request.supplierCode(), request.contractNumber()));
    }

    @Transactional
    public void deleteExternalSupplier(String itemCode, String supplierCode, String contractNumber) {
        log.info("Deleting external supplier itemCode={} supplierCode={} contract={}", itemCode, supplierCode, contractNumber);
        List<ExternalSupplierEntity> existing = consignmentSetupMapper.findExternalByItemCodeAndSupplier(itemCode, supplierCode, contractNumber);
        if (existing.isEmpty()) throw new ResourceNotFoundException("External supplier setup not found: " + supplierCode);
        for (ExternalSupplierEntity e : existing) {
            if (inventoryValidationMapper.countBlockingInventory(itemCode, e.getConsigneeStore()) > 0) {
                throw new BusinessRuleViolationException("Cannot delete while related inventory is not zero for store: " + e.getConsigneeStore());
            }
        }
        consignmentSetupMapper.deleteExternalBySupplier(itemCode, supplierCode, contractNumber);
    }

    // ── Internal supplier ──────────────────────────────────────────────────────

    @Transactional
    public InternalSupplierSetupResponse addInternalSupplier(String itemCode, InternalSupplierSetupRequest request) {
        log.info("Adding internal supplier itemCode={} supplierCode={} supplierStore={}", itemCode, request.supplierCode(), request.supplierStore());
        ensureItemExists(itemCode);
        ensureInternalHierarchy(itemCode, request.supplierStore());

        for (InternalSupplierSetupRequest.ConsigneeRequest consignee : request.consignees()) {
            for (String storeId : consignee.storeIds()) {
                InternalSupplierEntity entity = new InternalSupplierEntity();
                entity.setId(UUID.randomUUID().toString());
                entity.setItemCode(itemCode);
                entity.setSupplierCode(request.supplierCode());
                entity.setSupplierStore(request.supplierStore());
                entity.setConsigneeCompany(consignee.companyId());
                entity.setConsigneeStore(storeId);
                consignmentSetupMapper.insertInternal(entity);
            }
        }
        return toInternalGroupedResponse(request.supplierCode(), request.supplierStore(),
                consignmentSetupMapper.findInternalByItemCodeAndSupplier(itemCode, request.supplierCode(), request.supplierStore()));
    }

    @Transactional
    public InternalSupplierSetupResponse updateInternalSupplier(String itemCode, String supplierCode, String supplierStore,
                                                                  InternalSupplierSetupRequest request) {
        log.info("Updating internal supplier itemCode={} supplierCode={} supplierStore={}", itemCode, supplierCode, supplierStore);
        ensureItemExists(itemCode);
        ensureInternalHierarchy(itemCode, request.supplierStore());

        consignmentSetupMapper.deleteInternalBySupplier(itemCode, supplierCode, supplierStore);

        for (InternalSupplierSetupRequest.ConsigneeRequest consignee : request.consignees()) {
            for (String storeId : consignee.storeIds()) {
                InternalSupplierEntity entity = new InternalSupplierEntity();
                entity.setId(UUID.randomUUID().toString());
                entity.setItemCode(itemCode);
                entity.setSupplierCode(request.supplierCode());
                entity.setSupplierStore(request.supplierStore());
                entity.setConsigneeCompany(consignee.companyId());
                entity.setConsigneeStore(storeId);
                consignmentSetupMapper.insertInternal(entity);
            }
        }
        return toInternalGroupedResponse(request.supplierCode(), request.supplierStore(),
                consignmentSetupMapper.findInternalByItemCodeAndSupplier(itemCode, request.supplierCode(), request.supplierStore()));
    }

    @Transactional
    public void deleteInternalSupplier(String itemCode, String supplierCode, String supplierStore) {
        log.info("Deleting internal supplier itemCode={} supplierCode={} supplierStore={}", itemCode, supplierCode, supplierStore);
        List<InternalSupplierEntity> existing = consignmentSetupMapper.findInternalByItemCodeAndSupplier(itemCode, supplierCode, supplierStore);
        if (existing.isEmpty()) throw new ResourceNotFoundException("Internal supplier setup not found: " + supplierCode);
        consignmentSetupMapper.deleteInternalBySupplier(itemCode, supplierCode, supplierStore);
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

        // group external by supplierCode+contractNumber
        List<ExternalSupplierSetupResponse> externalGrouped = ext.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        e -> e.getSupplierCode() + "|" + e.getSupplierContract()))
                .entrySet().stream()
                .map(entry -> {
                    List<ExternalSupplierEntity> rows = entry.getValue();
                    ExternalSupplierEntity first = rows.get(0);
                    List<ExternalSupplierSetupResponse.ConsigneeGroup> consignees = rows.stream()
                            .collect(java.util.stream.Collectors.groupingBy(ExternalSupplierEntity::getConsigneeCompany))
                            .entrySet().stream()
                            .map(ce -> new ExternalSupplierSetupResponse.ConsigneeGroup(
                                    ce.getKey(),
                                    ce.getValue().stream().map(ExternalSupplierEntity::getConsigneeStore).toList()))
                            .toList();
                    return new ExternalSupplierSetupResponse(
                            first.getId(), first.getSupplierCode(), first.getSupplierType(),
                            first.getSupplierContract(), consignees);
                }).toList();

        // group internal by supplierCode+supplierStore
        List<InternalSupplierSetupResponse> internalGrouped = intl.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        i -> i.getSupplierCode() + "|" + i.getSupplierStore()))
                .entrySet().stream()
                .map(entry -> {
                    List<InternalSupplierEntity> rows = entry.getValue();
                    InternalSupplierEntity first = rows.get(0);
                    List<InternalSupplierSetupResponse.ConsigneeGroup> consignees = rows.stream()
                            .collect(java.util.stream.Collectors.groupingBy(InternalSupplierEntity::getConsigneeCompany))
                            .entrySet().stream()
                            .map(ce -> new InternalSupplierSetupResponse.ConsigneeGroup(
                                    ce.getKey(),
                                    ce.getValue().stream().map(InternalSupplierEntity::getConsigneeStore).toList()))
                            .toList();
                    return new InternalSupplierSetupResponse(
                            first.getId(), first.getSupplierCode(), first.getSupplierStore(), consignees);
                }).toList();

        return new ConsignmentSetupItemResponse(
                itemCode,
                item != null ? item.getItemName() : null,
                item != null ? item.getVariant() : null,
                item != null ? item.getHierarchy() : null,
                item != null ? item.getItemModel() : null,
                item != null ? item.getUnitRetail() : null,
                item != null ? item.getMvc() : null,
                category,
                externalGrouped,
                internalGrouped
        );
    }

    private ExternalSupplierSetupResponse toExternalGroupedResponse(String supplierCode, String supplierType,
                                                                      String contractNumber, List<ExternalSupplierEntity> rows) {
        String setupId = rows.isEmpty() ? null : rows.get(0).getId();
        List<ExternalSupplierSetupResponse.ConsigneeGroup> consignees = rows.stream()
                .collect(java.util.stream.Collectors.groupingBy(ExternalSupplierEntity::getConsigneeCompany))
                .entrySet().stream()
                .map(e -> new ExternalSupplierSetupResponse.ConsigneeGroup(
                        e.getKey(), e.getValue().stream().map(ExternalSupplierEntity::getConsigneeStore).toList()))
                .toList();
        return new ExternalSupplierSetupResponse(setupId, supplierCode, supplierType, contractNumber, consignees);
    }

    private InternalSupplierSetupResponse toInternalGroupedResponse(String supplierCode, String supplierStore,
                                                                      List<InternalSupplierEntity> rows) {
        String setupId = rows.isEmpty() ? null : rows.get(0).getId();
        List<InternalSupplierSetupResponse.ConsigneeGroup> consignees = rows.stream()
                .collect(java.util.stream.Collectors.groupingBy(InternalSupplierEntity::getConsigneeCompany))
                .entrySet().stream()
                .map(e -> new InternalSupplierSetupResponse.ConsigneeGroup(
                        e.getKey(), e.getValue().stream().map(InternalSupplierEntity::getConsigneeStore).toList()))
                .toList();
        return new InternalSupplierSetupResponse(setupId, supplierCode, supplierStore, consignees);
    }
}
