package com.consignment.service.service;

import com.consignment.service.exception.BusinessRuleViolationException;
import com.consignment.service.exception.ResourceNotFoundException;
import com.consignment.service.model.PageMeta;
import com.consignment.service.model.csr.CsrActualQtyUpdateRequest;
import com.consignment.service.model.csr.CsrDetailRequest;
import com.consignment.service.model.csr.CsrRequest;
import com.consignment.service.model.csr.CsrResponse;
import com.consignment.service.model.csr.CsrResponseDetail;
import com.consignment.service.model.csr.CsrSearchCriteria;
import com.consignment.service.persistence.mapper.CsrMapper;
import com.consignment.service.persistence.mapper.InventoryMutationMapper;
import com.consignment.service.persistence.model.CsrDetailEntity;
import com.consignment.service.persistence.model.CsrHeaderEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class CsrService {

    private static final String STATUS_HELD = "HELD";
    private static final String STATUS_RELEASED = "RELEASED";
    private static final String STATUS_COMPLETED = "COMPLETED";

    private final CsrMapper csrMapper;
    private final InventoryMutationMapper inventoryMutationMapper;
    private final NotificationService notificationService;

    public CsrService(
            CsrMapper csrMapper,
            InventoryMutationMapper inventoryMutationMapper,
            NotificationService notificationService
    ) {
        this.csrMapper = csrMapper;
        this.inventoryMutationMapper = inventoryMutationMapper;
        this.notificationService = notificationService;
    }

    @Transactional
    public CsrResponse create(CsrRequest request) {
        CsrHeaderEntity header = null;
        final int maxAttempts = 5;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            header = buildHeader(request);
            try {
                csrMapper.insertHeader(header);
                break;
            } catch (DataIntegrityViolationException ex) {
                if (isDocNoConflict(ex) && attempt < maxAttempts) {
                    continue;
                }
                throw ex;
            }
        }

        for (CsrDetailRequest item : request.items()) {
            CsrDetailEntity detail = new CsrDetailEntity();
            detail.setId(UUID.randomUUID().toString());
            detail.setCsrId(header.getId());
            detail.setItemCode(item.itemCode());
            detail.setUom(item.uom());
            detail.setQty(item.qty());
            detail.setActualQty(item.actualQty());
            csrMapper.insertDetail(detail);
        }

        return getById(header.getId());
    }

    private CsrHeaderEntity buildHeader(CsrRequest request) {
        CsrHeaderEntity header = new CsrHeaderEntity();
        header.setId(UUID.randomUUID().toString());
        header.setDocNo(nextDocNo());
        header.setCompany(request.company());
        header.setStore(request.store());
        header.setSupplierCode(request.supplierCode());
        header.setSupplierContract(request.supplierContract());
        header.setInternalSupplierStore(request.internalSupplierStore());
        header.setSupplierConfirmNote(request.supplierConfirmNote());
        header.setReasonCode(request.reasonCode());
        header.setRemark(request.remark());
        header.setStatus(STATUS_HELD);
        header.setCreatedBy(request.createdBy());
        header.setReferenceNo(request.referenceNo());
        header.setCsoDocNo(request.csoDocNo());
        return header;
    }

    public record PagedResult(List<CsrResponse> items, PageMeta meta) {}

    public PagedResult search(CsrSearchCriteria criteria) {
        List<CsrResponse> items = csrMapper.searchHeaders(criteria).stream()
                .map(h -> toResponse(h, csrMapper.findDetailsByHeaderId(h.getId())))
                .toList();
        long total = csrMapper.countHeaders(criteria);
        return new PagedResult(items, PageMeta.of(criteria.page(), criteria.perPage(), total));
    }

    public CsrResponse getById(String id) {
        CsrHeaderEntity header = csrMapper.findHeaderById(id);
        if (header == null) {
            throw new ResourceNotFoundException("CSR not found: " + id);
        }
        return toResponse(header, csrMapper.findDetailsByHeaderId(id));
    }

    @Transactional
    public CsrResponse release(String id) {
        CsrHeaderEntity header = csrMapper.findHeaderById(id);
        if (header == null) {
            throw new ResourceNotFoundException("CSR not found: " + id);
        }
        if (!STATUS_HELD.equalsIgnoreCase(header.getStatus())) {
            throw new BusinessRuleViolationException("Only CSR with status HELD can be released");
        }

        csrMapper.updateHeaderStatus(id, STATUS_RELEASED, Instant.now(), null);
        notificationService.sendCsrReleased(header.getDocNo(), header.getSupplierCode());
        return getById(id);
    }

    @Transactional
    public CsrResponse updateActualQty(String id, String detailId, CsrActualQtyUpdateRequest request) {
        CsrHeaderEntity header = csrMapper.findHeaderById(id);
        if (header == null) {
            throw new ResourceNotFoundException("CSR not found: " + id);
        }
        if (!STATUS_RELEASED.equalsIgnoreCase(header.getStatus())) {
            throw new BusinessRuleViolationException("Actual quantity can only be updated when CSR is RELEASED");
        }

        boolean detailExists = csrMapper.findDetailsByHeaderId(id).stream()
                .anyMatch(detail -> detail.getId().equals(detailId));
        if (!detailExists) {
            throw new ResourceNotFoundException("CSR detail not found: " + detailId);
        }

        csrMapper.updateActualQty(detailId, request.actualQty());
        return getById(id);
    }

    @Transactional
    public CsrResponse complete(String id) {
        CsrHeaderEntity header = csrMapper.findHeaderById(id);
        if (header == null) {
            throw new ResourceNotFoundException("CSR not found: " + id);
        }
        if (!STATUS_RELEASED.equalsIgnoreCase(header.getStatus())) {
            throw new BusinessRuleViolationException("Only CSR with status RELEASED can be completed");
        }

        List<CsrDetailEntity> details = csrMapper.findDetailsByHeaderId(id);
        for (CsrDetailEntity detail : details) {
            BigDecimal actualQty = detail.getActualQty() == null ? detail.getQty() : detail.getActualQty();
            inventoryMutationMapper.adjustSupplierClosing(
                    header.getStore(),
                    detail.getItemCode(),
                    header.getSupplierCode(),
                    header.getSupplierContract(),
                    actualQty.negate()
            );
            inventoryMutationMapper.upsertCustomerInventory(
                    header.getStore(),
                    null,
                    null,
                    detail.getItemCode(),
                    actualQty.negate()
            );
        }

        csrMapper.updateHeaderStatus(id, STATUS_COMPLETED, null, Instant.now());
        return getById(id);
    }

    private String nextDocNo() {
        Long maxDocNo = csrMapper.findMaxDocNoNumber();
        long nextNumber = (maxDocNo == null ? 0L : maxDocNo) + 1L;
        return "CSR-" + String.format("%05d", nextNumber);
    }

    private boolean isDocNoConflict(DataIntegrityViolationException ex) {
        String message = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
        return message != null && message.contains("csr_header_doc_no_key");
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private CsrResponse toResponse(CsrHeaderEntity header, List<CsrDetailEntity> details) {
        List<CsrResponseDetail> items = details.stream()
                .map(detail -> new CsrResponseDetail(
                        detail.getId(),
                        detail.getItemCode(),
                        detail.getUom(),
                        detail.getQty(),
                        detail.getActualQty()
                ))
                .toList();

        return new CsrResponse(
                header.getId(),
                header.getDocNo(),
                header.getCompany(),
                header.getStore(),
                header.getSupplierCode(),
                header.getSupplierContract(),
                header.getInternalSupplierStore(),
                header.getSupplierConfirmNote(),
                header.getReasonCode(),
                header.getRemark(),
                header.getStatus(),
                header.getCreatedBy(),
                header.getReferenceNo(),
                header.getCsoDocNo(),
                header.getReleasedAt(),
                header.getCompletedAt(),
                header.getCreatedAt(),
                header.getUpdatedAt(),
                items
        );
    }
}
