package com.consignment.service.service;

import com.consignment.service.exception.BusinessRuleViolationException;
import com.consignment.service.exception.ResourceNotFoundException;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class CsrService {

    private static final String STATUS_HELD = "HELD";
    private static final String STATUS_RELEASED = "RELEASED";
    private static final String STATUS_COMPLETED = "COMPLETED";

    private final CsrMapper csrMapper;
    private final InventoryMutationMapper inventoryMutationMapper;
    private final NotificationService notificationService;
    private final AtomicLong sequence = new AtomicLong(1);

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
        csrMapper.insertHeader(header);

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

    public List<CsrResponse> listAll() {
        return csrMapper.findAllHeaders().stream()
                .map(header -> toResponse(header, csrMapper.findDetailsByHeaderId(header.getId())))
                .toList();
    }

    public List<CsrResponse> search(CsrSearchCriteria criteria) {
        return csrMapper.searchHeaders(
                        normalize(criteria.company()),
                        normalize(criteria.store()),
                        normalize(criteria.supplierCode()),
                        normalize(criteria.supplierContract()),
                        normalize(criteria.status()),
                        normalize(criteria.createdBy()),
                        normalize(criteria.referenceNo()),
                        normalize(criteria.itemCode())
                ).stream()
                .map(header -> toResponse(header, csrMapper.findDetailsByHeaderId(header.getId())))
                .toList();
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
        return "CSR-" + String.format("%05d", sequence.getAndIncrement());
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
                header.getReleasedAt(),
                header.getCompletedAt(),
                header.getCreatedAt(),
                header.getUpdatedAt(),
                items
        );
    }
}
