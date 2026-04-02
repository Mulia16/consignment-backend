package com.consignment.service.service;

import com.consignment.service.exception.BusinessRuleViolationException;
import com.consignment.service.exception.ResourceNotFoundException;
import com.consignment.service.model.csrq.CsrqDetailRequest;
import com.consignment.service.model.csrq.CsrqRequest;
import com.consignment.service.model.csrq.CsrqResponse;
import com.consignment.service.model.csrq.CsrqResponseDetail;
import com.consignment.service.model.csrq.CsrqSearchCriteria;
import com.consignment.service.persistence.mapper.CsrqMapper;
import com.consignment.service.persistence.model.CsrqDetailEntity;
import com.consignment.service.persistence.model.CsrqHeaderEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class CsrqService {

    private static final String STATUS_HELD = "HELD";
    private static final String STATUS_RELEASED = "RELEASED";

    private final CsrqMapper csrqMapper;
    private final NotificationService notificationService;
    private final AtomicLong sequence = new AtomicLong(1);

    public CsrqService(CsrqMapper csrqMapper, NotificationService notificationService) {
        this.csrqMapper = csrqMapper;
        this.notificationService = notificationService;
    }

    @Transactional
    public CsrqResponse create(CsrqRequest request) {
        validateSetupRegistration(request);

        CsrqHeaderEntity header = new CsrqHeaderEntity();
        header.setId(UUID.randomUUID().toString());
        header.setDocNo(nextDocNo());
        header.setCompany(request.company());
        header.setStore(request.store());
        header.setSupplierCode(request.supplierCode());
        header.setSupplierContract(request.supplierContract());
        header.setBranch(request.branch());
        header.setInternalSupplierStore(request.internalSupplierStore());
        header.setNotes(request.notes());
        header.setStatus(STATUS_HELD);
        header.setCreatedBy(request.createdBy());
        header.setCreatedMethod(request.createdMethod());
        header.setReferenceNo(request.referenceNo());
        csrqMapper.insertHeader(header);

        for (CsrqDetailRequest item : request.items()) {
            CsrqDetailEntity detail = new CsrqDetailEntity();
            detail.setId(UUID.randomUUID().toString());
            detail.setCsrqId(header.getId());
            detail.setItemCode(item.itemCode());
            detail.setRequestQty(item.requestQty());
            detail.setRequestUom(item.requestUom());
            csrqMapper.insertDetail(detail);
        }

        return getById(header.getId());
    }

    public List<CsrqResponse> search(CsrqSearchCriteria criteria) {
        return csrqMapper.searchHeaders(
                        normalize(criteria.company()),
                        normalize(criteria.store()),
                        normalize(criteria.supplierCode()),
                        normalize(criteria.supplierContract()),
                        normalize(criteria.branch()),
                        normalize(criteria.internalSupplierStore()),
                        normalize(criteria.createdMethod()),
                        normalize(criteria.referenceNo()),
                        normalize(criteria.itemCode()),
                        normalize(criteria.status())
                ).stream()
                .map(header -> toResponse(header, csrqMapper.findDetailsByHeaderId(header.getId())))
                .toList();
    }

    public CsrqResponse getById(String id) {
        CsrqHeaderEntity header = loadHeader(id);
        return toResponse(header, csrqMapper.findDetailsByHeaderId(id));
    }

    @Transactional
    public CsrqResponse release(String id) {
        CsrqHeaderEntity header = loadHeader(id);
        if (!STATUS_HELD.equalsIgnoreCase(header.getStatus())) {
            throw new BusinessRuleViolationException("Only CSRQ with status HELD can be released");
        }

        csrqMapper.updateHeaderStatus(id, STATUS_RELEASED, Instant.now());
        notificationService.sendCsrqReleased(header.getDocNo(), header.getSupplierCode());
        return getById(id);
    }

    @Transactional
    public void delete(String id) {
        CsrqHeaderEntity header = loadHeader(id);
        if (!STATUS_HELD.equalsIgnoreCase(header.getStatus())) {
            throw new BusinessRuleViolationException("Only CSRQ with status HELD can be deleted");
        }
        csrqMapper.deleteHeader(id);
    }

    private void validateSetupRegistration(CsrqRequest request) {
        for (CsrqDetailRequest item : request.items()) {
            long matches = csrqMapper.countMatchingSetup(
                    item.itemCode(),
                    request.supplierCode(),
                    request.supplierContract(),
                    request.store(),
                    request.internalSupplierStore()
            );
            if (matches <= 0) {
                throw new BusinessRuleViolationException(
                        "Item " + item.itemCode() + " is not registered for supplier/contract/store in consignment setup"
                );
            }
        }
    }

    private CsrqHeaderEntity loadHeader(String id) {
        CsrqHeaderEntity header = csrqMapper.findHeaderById(id);
        if (header == null) {
            throw new ResourceNotFoundException("CSRQ not found: " + id);
        }
        return header;
    }

    private String nextDocNo() {
        return "CSRQ-" + String.format("%05d", sequence.getAndIncrement());
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private CsrqResponse toResponse(CsrqHeaderEntity header, List<CsrqDetailEntity> details) {
        List<CsrqResponseDetail> items = details.stream()
                .map(detail -> new CsrqResponseDetail(
                        detail.getId(),
                        detail.getItemCode(),
                        detail.getRequestQty(),
                        detail.getRequestUom()
                ))
                .toList();

        return new CsrqResponse(
                header.getId(),
                header.getDocNo(),
                header.getCompany(),
                header.getStore(),
                header.getSupplierCode(),
                header.getSupplierContract(),
                header.getBranch(),
                header.getInternalSupplierStore(),
                header.getNotes(),
                header.getStatus(),
                header.getCreatedBy(),
                header.getCreatedMethod(),
                header.getReferenceNo(),
                header.getReleasedAt(),
                header.getCreatedAt(),
                header.getUpdatedAt(),
                items
        );
    }
}
