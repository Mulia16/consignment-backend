package com.consignment.service.service;

import com.consignment.service.exception.BusinessRuleViolationException;
import com.consignment.service.exception.ResourceNotFoundException;
import com.consignment.service.model.PageMeta;
import com.consignment.service.model.csrn.*;
import com.consignment.service.persistence.mapper.CsrnMapper;
import com.consignment.service.persistence.model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CsrnService {

    private static final String STATUS_HELD = "HELD";
    private static final String STATUS_UPDATED = "UPDATED";

    private final CsrnMapper csrnMapper;

    public CsrnService(CsrnMapper csrnMapper) {
        this.csrnMapper = csrnMapper;
    }

    @Transactional
    public CsrnResponse create(CsrnRequest request) {
        CsrnHeaderEntity header = new CsrnHeaderEntity();
        header.setId(UUID.randomUUID().toString());
        header.setDocNo(nextDocNo());
        header.setCsoDocNo(request.csoDocNo());
        header.setCompany(request.company());
        header.setStore(request.store());
        header.setSupplierCode(request.supplierCode());
        header.setSupplierContract(request.supplierContract());
        header.setInternalSupplierStore(request.internalSupplierStore());
        header.setReasonCode(request.reasonCode());
        header.setRemark(request.remark());
        header.setStatus(STATUS_HELD);
        header.setCreatedBy(request.createdBy());
        header.setReferenceNo(request.referenceNo());
        csrnMapper.insertHeader(header);

        for (CsrnDetailRequest item : request.items()) {
            insertDetail(header.getId(), item);
        }

        return getById(header.getId());
    }

    @Transactional
    public CsrnResponse update(String id, CsrnUpdateRequest request) {
        CsrnHeaderEntity header = loadHeader(id);
        if (!STATUS_HELD.equalsIgnoreCase(header.getStatus())) {
            throw new BusinessRuleViolationException("Only CSRN with status HELD can be updated");
        }

        // Auto-create CSRN-C as snapshot before update
        createCsrnC(header);

        // Replace details
        csrnMapper.deleteDetails(id);
        for (CsrnDetailRequest item : request.items()) {
            insertDetail(id, item);
        }

        // Update header fields and set status UPDATED
        header.setReasonCode(request.reasonCode());
        header.setRemark(request.remark());
        csrnMapper.updateStatus(id, STATUS_UPDATED);

        return getById(id);
    }

    public record PagedResult(List<CsrnResponse> items, PageMeta meta) {}

    public PagedResult search(CsrnSearchCriteria criteria) {
        List<CsrnResponse> items = csrnMapper.searchHeaders(criteria).stream()
                .map(h -> toResponse(h, csrnMapper.findDetailsByHeaderId(h.getId())))
                .toList();
        long total = csrnMapper.countHeaders(criteria);
        return new PagedResult(items, PageMeta.of(criteria.page(), criteria.perPage(), total));
    }

    public CsrnResponse getById(String id) {
        CsrnHeaderEntity header = loadHeader(id);
        return toResponse(header, csrnMapper.findDetailsByHeaderId(id));
    }

    public CsrnCResponse getCsrnC(String csrnId) {
        CsrnCHeaderEntity cHeader = csrnMapper.findCHeaderByCsrnId(csrnId);
        if (cHeader == null) {
            throw new ResourceNotFoundException("CSRN-C not found for CSRN: " + csrnId);
        }
        List<CsrnCResponseDetail> items = csrnMapper.findCDetailsByCHeaderId(cHeader.getId()).stream()
                .map(d -> new CsrnCResponseDetail(d.getId(), d.getItemCode(), d.getUom(), d.getQty(), d.getActualQty()))
                .toList();
        return new CsrnCResponse(
                cHeader.getId(), cHeader.getDocNo(), cHeader.getCsrnId(), cHeader.getCsrnDocNo(),
                cHeader.getCsoDocNo(), cHeader.getCompany(), cHeader.getStore(),
                cHeader.getSupplierCode(), cHeader.getSupplierContract(),
                cHeader.getReasonCode(), cHeader.getRemark(), cHeader.getCreatedBy(),
                cHeader.getStatus(), cHeader.getCreatedAt(), cHeader.getUpdatedAt(), items
        );
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
        csrnMapper.insertCHeader(cHeader);

        for (CsrnDetailEntity d : details) {
            CsrnCDetailEntity cd = new CsrnCDetailEntity();
            cd.setId(UUID.randomUUID().toString());
            cd.setCsrnCId(cHeader.getId());
            cd.setItemCode(d.getItemCode());
            cd.setUom(d.getUom());
            cd.setQty(d.getQty());
            csrnMapper.insertCDetail(cd);
        }
    }

    private void insertDetail(String csrnId, CsrnDetailRequest item) {
        CsrnDetailEntity detail = new CsrnDetailEntity();
        detail.setId(UUID.randomUUID().toString());
        detail.setCsrnId(csrnId);
        detail.setItemCode(item.itemCode());
        detail.setUom(item.uom());
        detail.setQty(item.qty());
        csrnMapper.insertDetail(detail);
    }

    private CsrnHeaderEntity loadHeader(String id) {
        CsrnHeaderEntity header = csrnMapper.findHeaderById(id);
        if (header == null) throw new ResourceNotFoundException("CSRN not found: " + id);
        return header;
    }

    private String nextDocNo() {
        Long max = csrnMapper.findMaxDocNoNumber();
        return "CSRN-" + String.format("%05d", (max == null ? 0L : max) + 1L);
    }

    private String nextCDocNo() {
        Long max = csrnMapper.findMaxCDocNoNumber();
        return "CSRN-C-" + String.format("%05d", (max == null ? 0L : max) + 1L);
    }

    private CsrnResponse toResponse(CsrnHeaderEntity h, List<CsrnDetailEntity> details) {
        List<CsrnResponseDetail> items = details.stream()
                .map(d -> new CsrnResponseDetail(d.getId(), d.getItemCode(), d.getUom(), d.getQty()))
                .toList();
        return new CsrnResponse(h.getId(), h.getDocNo(), h.getCsoDocNo(), h.getCompany(), h.getStore(),
                h.getSupplierCode(), h.getSupplierContract(), h.getInternalSupplierStore(),
                h.getReasonCode(), h.getRemark(), h.getStatus(), h.getCreatedBy(), h.getReferenceNo(),
                h.getCreatedAt(), h.getUpdatedAt(), items);
    }
}
