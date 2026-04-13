package com.consignment.service.service;

import com.consignment.service.exception.BusinessRuleViolationException;
import com.consignment.service.exception.ResourceNotFoundException;
import com.consignment.service.model.PageMeta;
import com.consignment.service.model.billing.*;
import com.consignment.service.persistence.mapper.CustomerBillingMapper;
import com.consignment.service.persistence.mapper.CustomerBillingMapper.UnpostRow;
import com.consignment.service.persistence.model.CustomerBillingRequestDetailEntity;
import com.consignment.service.persistence.model.CustomerBillingRequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class CustomerBillingService {

    private static final String STATUS_HELD = "HELD";
    private static final String STATUS_RELEASED = "RELEASED";
    private static final String PROCESS_COMPLETED = "COMPLETED";

    private final CustomerBillingMapper mapper;
    private final PricingService pricingService;

    public CustomerBillingService(CustomerBillingMapper mapper, PricingService pricingService) {
        this.mapper = mapper;
        this.pricingService = pricingService;
    }

    public record PagedResult(List<CustomerBillingResponse> items, PageMeta meta) {}

    /**
     * Compute customer consignment billing request.
     * Reads unpost_staging_inventory for the store (filtered by customer if provided),
     * calculates billingQty = salesQty - returnQty, and creates a new billing document.
     */
    @Transactional
    public CustomerBillingResponse compute(CustomerBillingComputeRequest request) {
        // Query unpost staging inventory
        List<UnpostRow> unpostRows = mapper.findUnpostByStoreAndCustomer(
                request.store(), request.customerCode(),
                request.fromDate(), request.toDate());

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
        mapper.insertHeader(header);

        // Build details from unpost rows
        for (UnpostRow row : unpostRows) {
            BigDecimal salesQty  = row.salesQty()  != null ? row.salesQty()  : BigDecimal.ZERO;
            BigDecimal returnQty = row.returnQty() != null ? row.returnQty() : BigDecimal.ZERO;
            BigDecimal billingQty = salesQty.subtract(returnQty);

            // Resolve unit price
            BigDecimal unitPrice = pricingService.resolveUnitPrice(
                    row.sku(), null, request.store(), null, null, request.customerCode());

            BigDecimal lineAmount = unitPrice != null
                    ? billingQty.multiply(unitPrice)
                    : BigDecimal.ZERO;

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
            mapper.insertDetail(detail);
        }

        return getById(header.getId());
    }

    public PagedResult search(CustomerBillingSearchCriteria criteria) {
        List<CustomerBillingResponse> items = mapper.search(criteria).stream()
                .map(h -> toResponse(h, mapper.findDetailsByBillingId(h.getId())))
                .toList();
        long total = mapper.count(criteria);
        return new PagedResult(items, PageMeta.of(criteria.page(), criteria.perPage(), total));
    }

    public CustomerBillingResponse getById(String id) {
        CustomerBillingRequestEntity header = loadHeader(id);
        return toResponse(header, mapper.findDetailsByBillingId(id));
    }

    @Transactional
    public CustomerBillingResponse release(String id) {
        CustomerBillingRequestEntity header = loadHeader(id);
        if (!STATUS_HELD.equalsIgnoreCase(header.getStatus())) {
            throw new BusinessRuleViolationException("Only HELD billing request can be released");
        }
        mapper.updateStatus(id, STATUS_RELEASED, Instant.now());
        return getById(id);
    }

    @Transactional
    public CustomerBillingResponse updateActualReturnQty(String id, String detailId,
                                                          CustomerBillingActualReturnRequest request) {
        CustomerBillingRequestEntity header = loadHeader(id);
        if (!STATUS_RELEASED.equalsIgnoreCase(header.getStatus())) {
            throw new BusinessRuleViolationException("Actual return qty can only be updated after release");
        }
        boolean exists = mapper.findDetailsByBillingId(id).stream()
                .anyMatch(d -> d.getId().equals(detailId));
        if (!exists) throw new ResourceNotFoundException("Billing detail not found: " + detailId);
        mapper.updateActualReturnQty(detailId, request.actualReturnQty());
        return getById(id);
    }

    @Transactional
    public void delete(String id) {
        CustomerBillingRequestEntity header = loadHeader(id);
        if (!STATUS_HELD.equalsIgnoreCase(header.getStatus())) {
            throw new BusinessRuleViolationException("Only HELD billing request can be deleted");
        }
        mapper.deleteById(id);
    }

    private CustomerBillingRequestEntity loadHeader(String id) {
        CustomerBillingRequestEntity h = mapper.findById(id);
        if (h == null) throw new ResourceNotFoundException("Customer billing request not found: " + id);
        return h;
    }

    private String nextDocNo() {
        Long max = mapper.findMaxDocNoNumber();
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
