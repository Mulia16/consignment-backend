package com.consignment.service.service;

import com.consignment.service.constant.ConsignmentConstants;
import com.consignment.service.exception.BusinessRuleViolationException;
import com.consignment.service.exception.ResourceNotFoundException;
import com.consignment.service.model.PageMeta;
import com.consignment.service.model.csrn.*;
import com.consignment.service.persistence.mapper.CsrnCMapper;
import com.consignment.service.persistence.mapper.CsrnMapper;
import com.consignment.service.persistence.mapper.InventoryMutationMapper;
import com.consignment.service.persistence.model.CsrnDetailEntity;
import com.consignment.service.persistence.model.CsrnHeaderEntity;
import com.consignment.service.persistence.model.CsrnCDetailEntity;
import com.consignment.service.persistence.model.CsrnCHeaderEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class CsrnService {

    private static final String STATUS_HELD = ConsignmentConstants.STATUS_HELD;
    private static final String STATUS_RELEASED = "RELEASED";
    private static final String STATUS_COMPLETED = "COMPLETED";

    private final CsrnMapper csrnMapper;
    private final CsrnCMapper csrnCMapper;
    private final InventoryMutationMapper inventoryMutationMapper;
    private final NotificationService notificationService;

    public CsrnService(CsrnMapper csrnMapper, CsrnCMapper csrnCMapper,
                       InventoryMutationMapper inventoryMutationMapper,
                       NotificationService notificationService) {
        this.csrnMapper = csrnMapper;
        this.csrnCMapper = csrnCMapper;
        this.inventoryMutationMapper = inventoryMutationMapper;
        this.notificationService = notificationService;
    }

    public record PagedResult(List<CsrnResponse> items, PageMeta meta) {}

    @Transactional
    public CsrnResponse create(CsrnRequest request) {
        CsrnHeaderEntity header = null;
        for (int attempt = 1; attempt <= 5; attempt++) {
            header = buildHeader(request);
            try {
                csrnMapper.insertHeader(header);
                break;
            } catch (DataIntegrityViolationException ex) {
                if (isDocNoConflict(ex) && attempt < 5) continue;
                throw ex;
            }
        }
        for (CsrnDetailRequest item : request.items()) {
            csrnMapper.insertDetail(buildDetail(header.getId(), item));
        }
        return getById(header.getId());
    }

    @Transactional
    public CsrnResponse update(String id, CsrnUpdateRequest request) {
        CsrnHeaderEntity header = loadHeader(id);
        if (!STATUS_HELD.equalsIgnoreCase(header.getStatus())) {
            throw new BusinessRuleViolationException("Only CSRN with status HELD can be updated");
        }
        header.setInternalSupplierStore(request.internalSupplierStore());
        header.setSupplierConfirmNote(request.supplierConfirmNote());
        header.setReasonCode(request.reasonCode());
        header.setRemark(request.remark());
        header.setReferenceNo(request.referenceNo());
        if (request.csoDocNo() != null) header.setCsoDocNo(request.csoDocNo());
        csrnMapper.updateHeader(header);
        csrnMapper.deleteDetails(id);
        for (CsrnDetailRequest item : request.items()) {
            csrnMapper.insertDetail(buildDetail(id, item));
        }
        return getById(id);
    }

    public PagedResult search(CsrnSearchCriteria criteria) {
        List<CsrnResponse> items = csrnMapper.searchHeaders(criteria).stream()
                .map(h -> toResponse(h, csrnMapper.findDetailsByHeaderId(h.getId())))
                .toList();
        long total = csrnMapper.countHeaders(criteria);
        return new PagedResult(items, PageMeta.of(criteria.page(), criteria.perPage(), total));
    }

    public CsrnResponse getById(String id) {
        return toResponse(loadHeader(id), csrnMapper.findDetailsByHeaderId(id));
    }

    @Transactional
    public CsrnResponse release(String id) {
        CsrnHeaderEntity header = loadHeader(id);
        if (!STATUS_HELD.equalsIgnoreCase(header.getStatus())) {
            throw new BusinessRuleViolationException("Only CSRN with status HELD can be released");
        }
        csrnMapper.updateHeaderStatus(id, STATUS_RELEASED, Instant.now(), null);
        // Auto-create CSRN-C snapshot when CSRN is released
        createCsrnC(header);
        notificationService.sendCsrnReleased(header.getDocNo(), header.getSupplierCode());
        return getById(id);
    }

    @Transactional
    public CsrnResponse updateActualQty(String id, String detailId, CsrnActualQtyUpdateRequest request) {
        CsrnHeaderEntity header = loadHeader(id);
        if (!STATUS_RELEASED.equalsIgnoreCase(header.getStatus())) {
            throw new BusinessRuleViolationException("Actual quantity can only be updated when CSRN is RELEASED");
        }
        boolean exists = csrnMapper.findDetailsByHeaderId(id).stream()
                .anyMatch(d -> d.getId().equals(detailId));
        if (!exists) throw new ResourceNotFoundException("CSRN detail not found: " + detailId);
        csrnMapper.updateActualQty(detailId, request.actualQty());
        return getById(id);
    }

    @Transactional
    public CsrnResponse complete(String id) {
        CsrnHeaderEntity header = loadHeader(id);
        if (!STATUS_RELEASED.equalsIgnoreCase(header.getStatus())) {
            throw new BusinessRuleViolationException("Only CSRN with status RELEASED can be completed");
        }
        List<CsrnDetailEntity> details = csrnMapper.findDetailsByHeaderId(id);
        for (CsrnDetailEntity detail : details) {
            BigDecimal actualQty = detail.getActualQty() != null ? detail.getActualQty() : detail.getQty();
            inventoryMutationMapper.adjustSupplierClosing(
                    header.getStore(), detail.getItemCode(),
                    header.getSupplierCode(), header.getSupplierContract(), actualQty.negate());
            inventoryMutationMapper.upsertCustomerInventory(
                    header.getStore(), null, null, detail.getItemCode(), actualQty.negate());
        }
        csrnMapper.updateHeaderStatus(id, STATUS_COMPLETED, null, Instant.now());
        return getById(id);
    }

    private CsrnHeaderEntity loadHeader(String id) {
        CsrnHeaderEntity h = csrnMapper.findHeaderById(id);
        if (h == null) throw new ResourceNotFoundException("CSRN not found: " + id);
        return h;
    }

    private void createCsrnC(CsrnHeaderEntity header) {
        List<CsrnDetailEntity> details = csrnMapper.findDetailsByHeaderId(header.getId());
        CsrnCHeaderEntity cHeader = new CsrnCHeaderEntity();
        cHeader.setId(UUID.randomUUID().toString());
        cHeader.setDocNo(nextCDocNo());
        cHeader.setCsrnId(header.getId());
        cHeader.setCsrnDocNo(header.getDocNo());
        cHeader.setCsoDocNo(header.getCsoDocNo());
        cHeader.setCompany(header.getCompany());
        cHeader.setStore(header.getStore());
        cHeader.setSupplierCode(header.getSupplierCode());
        cHeader.setSupplierContract(header.getSupplierContract());
        cHeader.setInternalSupplierStore(header.getInternalSupplierStore());
        cHeader.setReasonCode(header.getReasonCode());
        cHeader.setRemark(header.getRemark());
        cHeader.setCreatedBy(header.getCreatedBy());
        csrnCMapper.insertCHeader(cHeader);
        for (CsrnDetailEntity d : details) {
            CsrnCDetailEntity cd = new CsrnCDetailEntity();
            cd.setId(UUID.randomUUID().toString());
            cd.setCsrnCId(cHeader.getId());
            cd.setItemCode(d.getItemCode());
            cd.setUom(d.getUom());
            cd.setQty(d.getQty());
            csrnCMapper.insertCDetail(cd);
        }
    }

    private String nextCDocNo() {
        Long max = csrnCMapper.findMaxCDocNoNumber();
        return "CSRN-C-" + String.format("%05d", (max == null ? 0L : max) + 1L);
    }

    private CsrnHeaderEntity buildHeader(CsrnRequest request) {
        CsrnHeaderEntity h = new CsrnHeaderEntity();
        h.setId(UUID.randomUUID().toString());
        h.setDocNo(nextDocNo());
        h.setCompany(request.company());
        h.setStore(request.store());
        h.setSupplierCode(request.supplierCode());
        h.setSupplierContract(request.supplierContract());
        h.setInternalSupplierStore(request.internalSupplierStore());
        h.setSupplierConfirmNote(request.supplierConfirmNote());
        h.setReasonCode(request.reasonCode());
        h.setRemark(request.remark());
        h.setStatus(STATUS_HELD);
        h.setCreatedBy(request.createdBy());
        h.setReferenceNo(request.referenceNo());
        h.setCsoDocNo(request.csoDocNo());
        return h;
    }

    private CsrnDetailEntity buildDetail(String csrnId, CsrnDetailRequest item) {
        CsrnDetailEntity d = new CsrnDetailEntity();
        d.setId(UUID.randomUUID().toString());
        d.setCsrnId(csrnId);
        d.setItemCode(item.itemCode());
        d.setUom(item.uom());
        d.setQty(item.qty());
        d.setActualQty(item.actualQty());
        return d;
    }

    private String nextDocNo() {
        Long max = csrnMapper.findMaxDocNoNumber();
        return "CSRN-" + String.format("%05d", (max == null ? 0L : max) + 1L);
    }

    private boolean isDocNoConflict(DataIntegrityViolationException ex) {
        String msg = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
        return msg != null && msg.contains("csrn_header_doc_no_key");
    }

    private CsrnResponse toResponse(CsrnHeaderEntity h, List<CsrnDetailEntity> details) {
        List<CsrnResponseDetail> items = details.stream()
                .map(d -> new CsrnResponseDetail(d.getId(), d.getItemCode(), d.getUom(), d.getQty(), d.getActualQty()))
                .toList();
        return new CsrnResponse(h.getId(), h.getDocNo(), h.getCompany(), h.getStore(),
                h.getSupplierCode(), h.getSupplierContract(), h.getInternalSupplierStore(),
                h.getSupplierConfirmNote(), h.getReasonCode(), h.getRemark(), h.getStatus(),
                h.getCreatedBy(), h.getReferenceNo(), h.getCsoDocNo(),
                h.getReleasedAt(), h.getCompletedAt(), h.getCreatedAt(), h.getUpdatedAt(), items);
    }
}
