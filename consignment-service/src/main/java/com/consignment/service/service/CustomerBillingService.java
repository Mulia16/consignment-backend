package com.consignment.service.service;

import com.consignment.service.exception.BusinessRuleViolationException;
import com.consignment.service.exception.ResourceNotFoundException;
import com.consignment.service.model.PageMeta;
import com.consignment.service.model.billing.*;
import com.consignment.service.persistence.mapper.ConsignmentUnpostMapper;
import com.consignment.service.persistence.mapper.ConsignmentUnpostMapper.UnpostAggRow;
import com.consignment.service.persistence.mapper.CustomerBillingMapper;
import com.consignment.service.persistence.model.CustomerBillingRequestDetailEntity;
import com.consignment.service.persistence.model.CustomerBillingRequestEntity;
import com.consignment.service.persistence.model.ConsignmentUnpostEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class CustomerBillingService {

    private static final String STATUS_HELD     = "HELD";
    private static final String STATUS_RELEASED = "RELEASED";
    private static final String PROCESS_COMPLETED = "COMPLETED";

    private final CustomerBillingMapper billingMapper;
    private final ConsignmentUnpostMapper unpostMapper;
    private final PricingService pricingService;

    public CustomerBillingService(CustomerBillingMapper billingMapper,
                                  ConsignmentUnpostMapper unpostMapper,
                                  PricingService pricingService) {
        this.billingMapper = billingMapper;
        this.unpostMapper  = unpostMapper;
        this.pricingService = pricingService;
    }

    public record PagedResult(List<CustomerBillingResponse> items, PageMeta meta) {}

    // ── ACMM Sync: receive unpost sales from POS / B2B / Online ──────────────

    @Transactional
    public int syncUnpostSales(UnpostSalesSyncRequest request) {
        for (UnpostSalesSyncRequest.UnpostSalesLine line : request.lines()) {
            ConsignmentUnpostEntity entity = new ConsignmentUnpostEntity();
            entity.setStore(line.store());
            entity.setSku(line.sku());
            entity.setSalesQty(line.salesQty());
            entity.setSalesReturnQty(line.salesReturnQty());
            entity.setSalesDate(line.salesDate());
            entity.setSourceType(line.sourceType());
            entity.setSourceRef(line.sourceRef());
            unpostMapper.insert(entity);
        }
        return request.lines().size();
    }

    // ── Compute CBR ───────────────────────────────────────────────────────────

    @Transactional
    public CustomerBillingResponse compute(CustomerBillingComputeRequest request) {
        // Prevent duplicate: check if CBR already exists for same store+period
        long existing = unpostMapper.countUnsettledCbr(
                request.store(), request.fromDate(), request.toDate());
        if (existing > 0) {
            throw new BusinessRuleViolationException(
                    "A billing request already exists for store " + request.store()
                    + " period " + request.fromDate() + " to " + request.toDate()
                    + ". Delete the existing HELD document before recomputing.");
        }

        // Aggregate unsettled unpost rows for this store+period
        List<UnpostAggRow> rows = unpostMapper.aggregateUnsettled(
                request.store(), request.fromDate(), request.toDate());

        // Build header
        CustomerBillingRequestEntity header = new CustomerBillingRequestEntity();
        header.setId(UUID.randomUUID().toString());
        header.setDocNo(nextDocNo());
        header.setPeriodType(request.periodType());
        header.setFromDate(request.fromDate());
        header.setToDate(request.toDate());
        header.setStore(request.store());
        header.setCustomerCode(request.customerCode());
        header.setStatus(STATUS_HELD);
        header.setProcessStatus(PROCESS_COMPLETED);
        header.setCreatedBy(request.createdBy());
        billingMapper.insertHeader(header);

        // Build detail lines from aggregated unpost
        for (UnpostAggRow row : rows) {
            BigDecimal salesQty   = row.totalSales()  != null ? row.totalSales()  : BigDecimal.ZERO;
            BigDecimal returnQty  = row.totalReturn() != null ? row.totalReturn() : BigDecimal.ZERO;
            BigDecimal billingQty = salesQty.subtract(returnQty);

            BigDecimal unitPrice = pricingService.resolveUnitPrice(
                    row.sku(), null, request.store(), null, null, request.customerCode());
            BigDecimal lineAmount = unitPrice != null
                    ? billingQty.multiply(unitPrice) : BigDecimal.ZERO;

            CustomerBillingRequestDetailEntity detail = new CustomerBillingRequestDetailEntity();
            detail.setId(UUID.randomUUID().toString());
            detail.setBillingId(header.getId());
            detail.setCustomerCode(request.customerCode());
            detail.setItemCode(row.sku());
            detail.setUom("PCS");
            detail.setSalesQty(salesQty);
            detail.setReturnQty(returnQty);
            detail.setBillingQty(billingQty);
            detail.setUnitPrice(unitPrice);
            detail.setLineAmount(lineAmount);
            billingMapper.insertDetail(detail);
        }

        return getById(header.getId());
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────

    public PagedResult search(CustomerBillingSearchCriteria criteria) {
        List<CustomerBillingResponse> items = billingMapper.search(criteria).stream()
                .map(h -> toResponse(h, billingMapper.findDetailsByBillingId(h.getId())))
                .toList();
        long total = billingMapper.count(criteria);
        return new PagedResult(items, PageMeta.of(criteria.page(), criteria.perPage(), total));
    }

    public CustomerBillingResponse getById(String id) {
        return toResponse(loadHeader(id), billingMapper.findDetailsByBillingId(id));
    }

    @Transactional
    public CustomerBillingResponse release(String id) {
        CustomerBillingRequestEntity header = loadHeader(id);
        if (!STATUS_HELD.equalsIgnoreCase(header.getStatus())) {
            throw new BusinessRuleViolationException("Only HELD billing request can be released");
        }
        billingMapper.updateStatus(id, STATUS_RELEASED, Instant.now());

        // Mark unpost rows as settled so they won't be included in next CBR
        unpostMapper.markSettled(header.getStore(), header.getFromDate(), header.getToDate(), id);

        return getById(id);
    }

    @Transactional
    public CustomerBillingResponse updateActualReturnQty(String id, String detailId,
                                                          CustomerBillingActualReturnRequest request) {
        CustomerBillingRequestEntity header = loadHeader(id);
        if (!STATUS_RELEASED.equalsIgnoreCase(header.getStatus())) {
            throw new BusinessRuleViolationException("Actual return qty can only be updated after release");
        }
        boolean exists = billingMapper.findDetailsByBillingId(id).stream()
                .anyMatch(d -> d.getId().equals(detailId));
        if (!exists) throw new ResourceNotFoundException("Billing detail not found: " + detailId);
        billingMapper.updateActualReturnQty(detailId, request.actualReturnQty());
        return getById(id);
    }

    @Transactional
    public void delete(String id) {
        CustomerBillingRequestEntity header = loadHeader(id);
        if (!STATUS_HELD.equalsIgnoreCase(header.getStatus())) {
            throw new BusinessRuleViolationException("Only HELD billing request can be deleted");
        }
        billingMapper.deleteById(id);
        // After delete, unpost rows remain unsettled — user can recompute
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private CustomerBillingRequestEntity loadHeader(String id) {
        CustomerBillingRequestEntity h = billingMapper.findById(id);
        if (h == null) throw new ResourceNotFoundException("Customer billing request not found: " + id);
        return h;
    }

    private String nextDocNo() {
        Long max = billingMapper.findMaxDocNoNumber();
        return "CBR-" + String.format("%05d", (max == null ? 0L : max) + 1L);
    }

    private CustomerBillingResponse toResponse(CustomerBillingRequestEntity h,
                                                List<CustomerBillingRequestDetailEntity> details) {
        List<CustomerBillingDetailResponse> detailResponses = details.stream()
                .map(d -> new CustomerBillingDetailResponse(
                        d.getId(), d.getCustomerCode(), d.getCustomerBranch(),
                        d.getItemCode(), d.getUom(), d.getSalesQty(), d.getReturnQty(),
                        d.getBillingQty(), d.getUnitPrice(), d.getLineAmount(), d.getActualReturnQty()))
                .toList();
        return new CustomerBillingResponse(
                h.getId(), h.getDocNo(), h.getPeriodType(), h.getFromDate(), h.getToDate(),
                h.getStore(), h.getCustomerCode(), h.getCustomerBranch(),
                h.getStatus(), h.getProcessStatus(), h.getCreatedBy(),
                h.getReleasedAt(), h.getCreatedAt(), h.getUpdatedAt(), detailResponses);
    }
}
