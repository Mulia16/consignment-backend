package com.consignment.service.service;

import com.consignment.service.constant.ConsignmentConstants;
import com.consignment.service.exception.BusinessRuleViolationException;
import com.consignment.service.exception.ResourceNotFoundException;
import com.consignment.service.model.PageMeta;
import com.consignment.service.model.csrn.*;
import com.consignment.service.persistence.mapper.CsrnCMapper;
import com.consignment.service.persistence.mapper.CsrnMapper;
import com.consignment.service.persistence.mapper.InventoryMutationMapper;
import com.consignment.service.persistence.model.CsrnCDetailEntity;
import com.consignment.service.persistence.model.CsrnCHeaderEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CsrnCService {

    private static final String STATUS_HELD = ConsignmentConstants.STATUS_HELD;
    private static final String STATUS_UPDATED = "UPDATED";

    private final CsrnCMapper csrnCMapper;
    private final InventoryMutationMapper inventoryMutationMapper;

    public CsrnCService(CsrnCMapper csrnCMapper, InventoryMutationMapper inventoryMutationMapper) {
        this.csrnCMapper = csrnCMapper;
        this.inventoryMutationMapper = inventoryMutationMapper;
    }

    public record PagedResult(List<CsrnCResponse> items, PageMeta meta) {}

    public PagedResult search(CsrnCSearchCriteria criteria) {
        List<CsrnCResponse> items = csrnCMapper.searchCHeaders(criteria).stream()
                .map(this::toCsrnCResponse)
                .toList();
        long total = csrnCMapper.countCHeaders(criteria);
        return new PagedResult(items, PageMeta.of(criteria.page(), criteria.perPage(), total));
    }

    public CsrnCResponse getById(String id) {
        CsrnCHeaderEntity header = csrnCMapper.findCHeaderById(id);
        if (header == null) throw new ResourceNotFoundException("CSRN-C not found: " + id);
        return toCsrnCResponse(header);
    }

    @Transactional
    public CsrnCResponse updateActualQty(String id, String detailId, CsrnCActualQtyRequest request) {
        CsrnCHeaderEntity header = csrnCMapper.findCHeaderById(id);
        if (header == null) throw new ResourceNotFoundException("CSRN-C not found: " + id);
        if (!STATUS_HELD.equalsIgnoreCase(header.getStatus())) {
            throw new BusinessRuleViolationException("Actual quantity can only be updated when CSRN-C is HELD");
        }

        boolean detailExists = csrnCMapper.findCDetailsByCHeaderId(id).stream()
                .anyMatch(d -> d.getId().equals(detailId));
        if (!detailExists) throw new ResourceNotFoundException("CSRN-C detail not found: " + detailId);

        csrnCMapper.updateCActualQty(detailId, request.actualQty());
        return getById(id);
    }

    @Transactional
    public CsrnCResponse complete(String id) {
        CsrnCHeaderEntity header = csrnCMapper.findCHeaderById(id);
        if (header == null) throw new ResourceNotFoundException("CSRN-C not found: " + id);
        if (!STATUS_HELD.equalsIgnoreCase(header.getStatus())) {
            throw new BusinessRuleViolationException("Only CSRN-C with status HELD can be completed");
        }

        List<CsrnCDetailEntity> details = csrnCMapper.findCDetailsByCHeaderId(id);
        boolean isInternal = header.getInternalSupplierStore() != null;

        for (CsrnCDetailEntity detail : details) {
            BigDecimal actualQty = detail.getActualQty() != null ? detail.getActualQty() : detail.getQty();
            inventoryMutationMapper.adjustSupplierClosing(
                    header.getStore(), detail.getItemCode(),
                    header.getSupplierCode(), header.getSupplierContract(), actualQty.negate());
            if (isInternal) {
                inventoryMutationMapper.upsertCustomerInventory(
                        header.getStore(), null, null, detail.getItemCode(), actualQty.negate());
            }
        }

        csrnCMapper.updateCStatus(id, STATUS_UPDATED);
        return getById(id);
    }

    private CsrnCResponse toCsrnCResponse(CsrnCHeaderEntity h) {
        List<CsrnCResponseDetail> items = csrnCMapper.findCDetailsByCHeaderId(h.getId()).stream()
                .map(d -> new CsrnCResponseDetail(d.getId(), d.getItemCode(), d.getUom(), d.getQty(), d.getActualQty()))
                .toList();
        return new CsrnCResponse(
                h.getId(), h.getDocNo(), h.getCsrnId(), h.getCsrnDocNo(),
                h.getCsoDocNo(), h.getCompany(), h.getStore(),
                h.getSupplierCode(), h.getSupplierContract(),
                h.getReasonCode(), h.getRemark(), h.getCreatedBy(),
                h.getStatus(), h.getCreatedAt(), h.getUpdatedAt(), items
        );
    }
}
