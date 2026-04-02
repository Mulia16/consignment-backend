package com.consignment.service.service;

import com.consignment.service.exception.BusinessRuleViolationException;
import com.consignment.service.exception.ResourceNotFoundException;
import com.consignment.service.model.csa.CsaDetailRequest;
import com.consignment.service.model.csa.CsaRequest;
import com.consignment.service.model.csa.CsaResponse;
import com.consignment.service.model.csa.CsaResponseDetail;
import com.consignment.service.model.csa.CsaSearchCriteria;
import com.consignment.service.persistence.mapper.CsaMapper;
import com.consignment.service.persistence.mapper.InventoryMutationMapper;
import com.consignment.service.persistence.model.CsaDetailEntity;
import com.consignment.service.persistence.model.CsaHeaderEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class CsaService {

    private static final String STATUS_HELD = "HELD";
    private static final String STATUS_RELEASED = "RELEASED";
    private static final String TRANS_TYPE_ADJ_IN = "ADJ_IN";
    private static final String TRANS_TYPE_ADJ_OUT = "ADJ_OUT";
    private static final String DECISION_UNPOST_RETURN = "UNPOST_RETURN";
    private static final String DECISION_BV_INCREASE = "DIRECT_BV_INCREASE";
    private static final String DECISION_UNPOST_SALES = "UNPOST_SALES";
    private static final String DECISION_BV_DEDUCT = "DIRECT_BV_DEDUCT";

    private final CsaMapper csaMapper;
    private final InventoryMutationMapper inventoryMutationMapper;
    private final AtomicLong sequence = new AtomicLong(1);

    public CsaService(CsaMapper csaMapper, InventoryMutationMapper inventoryMutationMapper) {
        this.csaMapper = csaMapper;
        this.inventoryMutationMapper = inventoryMutationMapper;
    }

    @Transactional
    public CsaResponse create(CsaRequest request) {
        validateTransactionType(request.transactionType());

        CsaHeaderEntity header = new CsaHeaderEntity();
        header.setId(UUID.randomUUID().toString());
        header.setDocNo(nextDocNo());
        header.setCompany(request.company());
        header.setStore(request.store());
        header.setSupplierCode(request.supplierCode());
        header.setSupplierContract(request.supplierContract());
        header.setTransactionType(request.transactionType());
        header.setReferenceNo(request.referenceNo());
        header.setReasonCode(request.reasonCode());
        header.setRemark(request.remark());
        header.setCreateFrom(request.createFrom());
        header.setStatus(STATUS_HELD);
        header.setCreatedBy(request.createdBy());
        csaMapper.insertHeader(header);

        for (CsaDetailRequest item : request.items()) {
            CsaDetailEntity detail = new CsaDetailEntity();
            detail.setId(UUID.randomUUID().toString());
            detail.setCsaId(header.getId());
            detail.setItemCode(item.itemCode());
            detail.setItemName(item.itemName());
            detail.setQty(item.qty());
            detail.setUom(item.uom());
            detail.setSettlementDecision(item.settlementDecision());
            csaMapper.insertDetail(detail);
        }

        return getById(header.getId());
    }

    public List<CsaResponse> listAll() {
        return csaMapper.findAllHeaders().stream()
                .map(header -> toResponse(header, csaMapper.findDetailsByHeaderId(header.getId())))
                .toList();
    }

    public List<CsaResponse> search(CsaSearchCriteria criteria) {
        return csaMapper.searchHeaders(
                        normalize(criteria.company()),
                        normalize(criteria.store()),
                        normalize(criteria.transactionType()),
                        normalize(criteria.status()),
                        normalize(criteria.createdBy()),
                        normalize(criteria.referenceNo())
                ).stream()
                .map(header -> toResponse(header, csaMapper.findDetailsByHeaderId(header.getId())))
                .toList();
    }

    public CsaResponse getById(String id) {
        CsaHeaderEntity header = csaMapper.findHeaderById(id);
        if (header == null) {
            throw new ResourceNotFoundException("CSA not found: " + id);
        }
        return toResponse(header, csaMapper.findDetailsByHeaderId(id));
    }

    @Transactional
    public CsaResponse release(String id, String releasedBy) {
        CsaHeaderEntity header = csaMapper.findHeaderById(id);
        if (header == null) {
            throw new ResourceNotFoundException("CSA not found: " + id);
        }
        if (!STATUS_HELD.equalsIgnoreCase(header.getStatus())) {
            throw new BusinessRuleViolationException("Only CSA with status HELD can be released");
        }

        List<CsaDetailEntity> details = csaMapper.findDetailsByHeaderId(id);
        for (CsaDetailEntity detail : details) {
            applySettlement(header, detail);
        }

        csaMapper.updateHeaderStatus(id, STATUS_RELEASED, Instant.now(), releasedBy);
        return getById(id);
    }

    private void applySettlement(CsaHeaderEntity header, CsaDetailEntity detail) {
        String transType = header.getTransactionType().toUpperCase();
        String decision = detail.getSettlementDecision().toUpperCase();
        BigDecimal qty = detail.getQty();

        if (TRANS_TYPE_ADJ_IN.equals(transType)) {
            if (DECISION_UNPOST_RETURN.equals(decision)) {
                inventoryMutationMapper.upsertUnpost(detail.getItemCode(), header.getStore(), BigDecimal.ZERO, qty);
                return;
            }
            if (DECISION_BV_INCREASE.equals(decision)) {
                inventoryMutationMapper.adjustSupplierClosing(
                        header.getStore(),
                        detail.getItemCode(),
                        header.getSupplierCode(),
                        header.getSupplierContract(),
                        qty
                );
                return;
            }
            throw new BusinessRuleViolationException("Invalid ADJ_IN settlement decision: " + detail.getSettlementDecision());
        }

        if (TRANS_TYPE_ADJ_OUT.equals(transType)) {
            if (DECISION_UNPOST_SALES.equals(decision)) {
                inventoryMutationMapper.upsertUnpost(detail.getItemCode(), header.getStore(), qty, BigDecimal.ZERO);
                return;
            }
            if (DECISION_BV_DEDUCT.equals(decision)) {
                inventoryMutationMapper.adjustSupplierClosing(
                        header.getStore(),
                        detail.getItemCode(),
                        header.getSupplierCode(),
                        header.getSupplierContract(),
                        qty.negate()
                );
                return;
            }
            throw new BusinessRuleViolationException("Invalid ADJ_OUT settlement decision: " + detail.getSettlementDecision());
        }

        throw new BusinessRuleViolationException("Unsupported transaction type: " + header.getTransactionType());
    }

    private void validateTransactionType(String transactionType) {
        String value = transactionType.toUpperCase();
        if (!TRANS_TYPE_ADJ_IN.equals(value) && !TRANS_TYPE_ADJ_OUT.equals(value)) {
            throw new BusinessRuleViolationException("transactionType must be ADJ_IN or ADJ_OUT");
        }
    }

    private String nextDocNo() {
        return "CSA-" + String.format("%05d", sequence.getAndIncrement());
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private CsaResponse toResponse(CsaHeaderEntity header, List<CsaDetailEntity> details) {
        List<CsaResponseDetail> items = details.stream()
                .map(detail -> new CsaResponseDetail(
                        detail.getId(),
                        detail.getItemCode(),
                        detail.getItemName(),
                        detail.getQty(),
                        detail.getUom(),
                        detail.getSettlementDecision()
                ))
                .toList();

        return new CsaResponse(
                header.getId(),
                header.getDocNo(),
                header.getCompany(),
                header.getStore(),
                header.getSupplierCode(),
                header.getSupplierContract(),
                header.getTransactionType(),
                header.getReferenceNo(),
                header.getReasonCode(),
                header.getRemark(),
                header.getCreateFrom(),
                header.getStatus(),
                header.getCreatedBy(),
                header.getReleasedAt(),
                header.getReleasedBy(),
                header.getCreatedAt(),
                header.getUpdatedAt(),
                items
        );
    }
}
