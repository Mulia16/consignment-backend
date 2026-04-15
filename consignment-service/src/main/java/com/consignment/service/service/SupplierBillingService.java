package com.consignment.service.service;

import com.consignment.service.exception.BusinessRuleViolationException;
import com.consignment.service.exception.ResourceNotFoundException;
import com.consignment.service.model.PageMeta;
import com.consignment.service.model.billing.*;
import com.consignment.service.persistence.mapper.ConsignmentUnpostMapper;
import com.consignment.service.persistence.mapper.ConsignmentUnpostMapper.SupplierUnpostAggRow;
import com.consignment.service.persistence.mapper.InventoryMutationMapper;
import com.consignment.service.persistence.mapper.ItemPriceMapper;
import com.consignment.service.persistence.mapper.SupplierBillingMapper;
import com.consignment.service.persistence.mapper.SupplierBookValueInventoryMapper;
import com.consignment.service.persistence.model.ItemPriceEntity;
import com.consignment.service.persistence.model.SupplierBillingRequestDetailEntity;
import com.consignment.service.persistence.model.SupplierBillingRequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class SupplierBillingService {

    private static final String STATUS_HELD      = "HELD";
    private static final String STATUS_RELEASED  = "RELEASED";
    private static final String PROCESS_COMPLETED = "COMPLETED";
    private static final String PROCESS_FAILED    = "FAILED";

    private final SupplierBillingMapper billingMapper;
    private final ConsignmentUnpostMapper unpostMapper;
    private final InventoryMutationMapper inventoryMutationMapper;
    private final ItemPriceMapper itemPriceMapper;
    private final SupplierBookValueInventoryMapper supplierBookValueMapper;

    public SupplierBillingService(SupplierBillingMapper billingMapper,
                                  ConsignmentUnpostMapper unpostMapper,
                                  InventoryMutationMapper inventoryMutationMapper,
                                  ItemPriceMapper itemPriceMapper,
                                  SupplierBookValueInventoryMapper supplierBookValueMapper) {
        this.billingMapper          = billingMapper;
        this.unpostMapper           = unpostMapper;
        this.inventoryMutationMapper = inventoryMutationMapper;
        this.itemPriceMapper        = itemPriceMapper;
        this.supplierBookValueMapper = supplierBookValueMapper;
    }

    public record PagedResult(List<SupplierBillingResponse> items, PageMeta meta) {}

    // ── Compute SCBR ──────────────────────────────────────────────────────────

    @Transactional
    public SupplierBillingResponse compute(SupplierBillingComputeRequest request) {
        // Duplicate prevention
        long existing = unpostMapper.countUnsettledScbr(
                request.store(), request.supplierCode(), request.fromDate(), request.toDate());
        if (existing > 0) {
            throw new BusinessRuleViolationException(
                    "A supplier billing request already exists for supplier " + request.supplierCode()
                    + " period " + request.fromDate() + " to " + request.toDate()
                    + ". Delete the existing HELD document before recomputing.");
        }

        // Aggregate unsettled unpost rows
        List<SupplierUnpostAggRow> rows = unpostMapper.aggregateUnsettledBySupplier(
                request.store(), request.supplierCode(), request.fromDate(), request.toDate());

        // Build header
        SupplierBillingRequestEntity header = new SupplierBillingRequestEntity();
        header.setId(UUID.randomUUID().toString());
        header.setDocNo(nextDocNo());
        header.setCompany(request.company());
        header.setPeriodType(request.periodType());
        header.setFromDate(request.fromDate());
        header.setToDate(request.toDate());
        header.setStore(request.store());
        header.setSupplierCode(request.supplierCode());
        header.setCarryForwardDecimal(request.carryForwardDecimal());
        header.setProcessDate(Instant.now());
        header.setCreatedBy(request.createdBy());

        if (rows.isEmpty()) {
            header.setStatus(STATUS_HELD);
            header.setProcessStatus(PROCESS_FAILED);
            header.setErrorReason("No unsettled consignment unpost data found for supplier "
                    + request.supplierCode() + " in period " + request.fromDate() + " to " + request.toDate());
            billingMapper.insertHeader(header);
            return getById(header.getId());
        }

        header.setStatus(STATUS_HELD);
        header.setProcessStatus(PROCESS_COMPLETED);
        billingMapper.insertHeader(header);

        for (SupplierUnpostAggRow row : rows) {
            BigDecimal salesQty   = row.totalSales()  != null ? row.totalSales()  : BigDecimal.ZERO;
            BigDecimal returnQty  = row.totalReturn() != null ? row.totalReturn() : BigDecimal.ZERO;

            // BF_Qty from last RELEASED SCBR CF_Qty for same store+supplier+item
            BigDecimal bfQty = billingMapper.findLastCfQty(row.store(), request.supplierCode(), row.sku());
            if (bfQty == null) bfQty = BigDecimal.ZERO;

            BigDecimal rawBillingQty = salesQty.subtract(returnQty).add(bfQty);

            BigDecimal cfQty;
            BigDecimal effectiveBillingQty;
            if (request.carryForwardDecimal() && rawBillingQty.scale() > 0
                    && rawBillingQty.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) != 0) {
                // Extract decimal portion as CF_Qty
                cfQty = rawBillingQty.remainder(BigDecimal.ONE).abs();
                effectiveBillingQty = rawBillingQty.setScale(0, RoundingMode.FLOOR);
            } else {
                cfQty = BigDecimal.ZERO;
                effectiveBillingQty = rawBillingQty;
            }

            // Unit cost from item price master
            BigDecimal unitCost = resolveUnitCost(row.sku(), request.company(), row.store(),
                    request.supplierCode(), null);
            BigDecimal totalCost = unitCost != null
                    ? effectiveBillingQty.multiply(unitCost) : BigDecimal.ZERO;

            // Total supplier qty from book value inventory (display only)
            BigDecimal totalSupplierQty = supplierBookValueMapper.findClosingQty(
                    row.store(), row.sku(), request.supplierCode(), null);

            SupplierBillingRequestDetailEntity detail = new SupplierBillingRequestDetailEntity();
            detail.setId(UUID.randomUUID().toString());
            detail.setBillingId(header.getId());
            detail.setItemCode(row.sku());
            detail.setUom("PCS");
            detail.setSalesQty(salesQty);
            detail.setSalesReturnQty(returnQty);
            detail.setBfQty(bfQty);
            detail.setBillingQty(effectiveBillingQty);
            detail.setCfQty(cfQty);
            detail.setUnitCost(unitCost);
            detail.setTotalCost(totalCost);
            detail.setTotalSupplierQty(totalSupplierQty != null ? totalSupplierQty : BigDecimal.ZERO);
            billingMapper.insertDetail(detail);
        }

        return getById(header.getId());
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────

    public PagedResult search(SupplierBillingSearchCriteria criteria) {
        List<SupplierBillingResponse> items = billingMapper.search(criteria).stream()
                .map(h -> toResponse(h, billingMapper.findDetailsByBillingId(h.getId())))
                .toList();
        long total = billingMapper.count(criteria);
        return new PagedResult(items, PageMeta.of(criteria.page(), criteria.perPage(), total));
    }

    public SupplierBillingResponse getById(String id) {
        return toResponse(loadHeader(id), billingMapper.findDetailsByBillingId(id));
    }

    @Transactional
    public SupplierBillingResponse release(String id) {
        SupplierBillingRequestEntity header = loadHeader(id);
        if (!STATUS_HELD.equalsIgnoreCase(header.getStatus())) {
            throw new BusinessRuleViolationException("Only HELD supplier billing request can be released");
        }

        // Update status
        billingMapper.updateStatus(id, STATUS_RELEASED, Instant.now());

        // Mark unpost rows as settled
        unpostMapper.markSettledByScbr(header.getStore(), header.getFromDate(), header.getToDate(), id);

        // Deduct inventories per detail line
        List<SupplierBillingRequestDetailEntity> details = billingMapper.findDetailsByBillingId(id);
        for (SupplierBillingRequestDetailEntity detail : details) {
            if (detail.getBillingQty() == null || detail.getBillingQty().compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }
            // Deduct customer consignment inventory (negative delta = deduct)
            inventoryMutationMapper.upsertCustomerInventory(
                    header.getStore(), null, null,
                    detail.getItemCode(), detail.getBillingQty().negate());

            // Deduct supplier book value inventory closing qty
            inventoryMutationMapper.adjustSupplierClosing(
                    header.getStore(), detail.getItemCode(),
                    header.getSupplierCode(), header.getSupplierContract(),
                    detail.getBillingQty().negate());
        }

        return getById(id);
    }

    @Transactional
    public void delete(String id) {
        SupplierBillingRequestEntity header = loadHeader(id);
        if (!STATUS_HELD.equalsIgnoreCase(header.getStatus())) {
            throw new BusinessRuleViolationException("Only HELD supplier billing request can be deleted");
        }
        billingMapper.deleteById(id);
        // Unpost rows remain unsettled — user can recompute
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private SupplierBillingRequestEntity loadHeader(String id) {
        SupplierBillingRequestEntity h = billingMapper.findById(id);
        if (h == null) throw new ResourceNotFoundException("Supplier billing request not found: " + id);
        return h;
    }

    private String nextDocNo() {
        Long max = billingMapper.findMaxDocNoNumber();
        return "SCBR-" + String.format("%05d", (max == null ? 0L : max) + 1L);
    }

    private BigDecimal resolveUnitCost(String itemCode, String company, String store,
                                       String supplierCode, String supplierContract) {
        ItemPriceEntity price = itemPriceMapper.findEffectivePrice(
                itemCode, company, store, supplierCode, supplierContract, null);
        return price != null ? price.getUnitPrice() : null;
    }

    private SupplierBillingResponse toResponse(SupplierBillingRequestEntity h,
                                                List<SupplierBillingRequestDetailEntity> details) {
        List<SupplierBillingDetailResponse> detailResponses = details.stream()
                .map(d -> new SupplierBillingDetailResponse(
                        d.getId(), d.getItemCode(), d.getUom(),
                        d.getSalesQty(), d.getSalesReturnQty(), d.getBfQty(),
                        d.getBillingQty(), d.getCfQty(),
                        d.getUnitCost(), d.getTotalCost(), d.getTotalSupplierQty()))
                .toList();
        return new SupplierBillingResponse(
                h.getId(), h.getDocNo(), h.getCompany(), h.getPeriodType(),
                h.getFromDate(), h.getToDate(), h.getStore(),
                h.getSupplierCode(), h.getSupplierContract(), h.getSupplierType(),
                h.isCarryForwardDecimal(), h.getStatus(), h.getProcessStatus(),
                h.getErrorReason(), h.getProcessDate(), h.getCreatedBy(),
                h.getReleasedAt(), h.getCreatedAt(), h.getUpdatedAt(),
                detailResponses);
    }
}
