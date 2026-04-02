package com.consignment.service.service;

import com.consignment.service.exception.BusinessRuleViolationException;
import com.consignment.service.exception.ResourceNotFoundException;
import com.consignment.service.model.csrv.CsrvDetailRequest;
import com.consignment.service.model.csrv.CsrvRequest;
import com.consignment.service.model.csrv.CsrvResponse;
import com.consignment.service.model.csrv.CsrvResponseDetail;
import com.consignment.service.model.csrv.CsrvSearchCriteria;
import com.consignment.service.persistence.mapper.CsrvMapper;
import com.consignment.service.persistence.mapper.SupplierBookValueInventoryMapper;
import com.consignment.service.persistence.model.CsrvDetailEntity;
import com.consignment.service.persistence.model.CsrvHeaderEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class CsrvService {

    private static final String STATUS_HELD = "HELD";
    private static final String STATUS_RELEASED = "RELEASED";
    private static final String METHOD_API = "API";

    private final CsrvMapper csrvMapper;
    private final SupplierBookValueInventoryMapper supplierBookValueInventoryMapper;
    private final AtomicLong sequence = new AtomicLong(1);

    public CsrvService(
            CsrvMapper csrvMapper,
            SupplierBookValueInventoryMapper supplierBookValueInventoryMapper
    ) {
        this.csrvMapper = csrvMapper;
        this.supplierBookValueInventoryMapper = supplierBookValueInventoryMapper;
    }

    @Transactional
    public CsrvResponse create(CsrvRequest request) {
        validateSetupRegistration(request);
        return persist(request);
    }

    @Transactional
    public CsrvResponse autoCreate(CsrvRequest request) {
        if (!METHOD_API.equalsIgnoreCase(request.createdMethod())) {
            throw new BusinessRuleViolationException("Auto-create CSRV requires createdMethod = API");
        }
        validateSetupRegistration(request);
        return persist(request);
    }

    public List<CsrvResponse> search(CsrvSearchCriteria criteria) {
        return csrvMapper.searchHeaders(
                        normalize(criteria.company()),
                        normalize(criteria.receivingStore()),
                        normalize(criteria.supplierCode()),
                        normalize(criteria.supplierContract()),
                        normalize(criteria.branch()),
                        normalize(criteria.createdMethod()),
                        normalize(criteria.referenceNo()),
                        normalize(criteria.itemCode()),
                        normalize(criteria.status())
                ).stream()
                .map(header -> toResponse(header, csrvMapper.findDetailsByHeaderId(header.getId())))
                .toList();
    }

    public CsrvResponse getById(String id) {
        CsrvHeaderEntity header = loadHeader(id);
        return toResponse(header, csrvMapper.findDetailsByHeaderId(id));
    }

    @Transactional
    public CsrvResponse release(String id) {
        CsrvHeaderEntity header = loadHeader(id);
        if (!STATUS_HELD.equalsIgnoreCase(header.getStatus())) {
            throw new BusinessRuleViolationException("Only CSRV with status HELD can be released");
        }

        List<CsrvDetailEntity> details = csrvMapper.findDetailsByHeaderId(id);
        for (CsrvDetailEntity detail : details) {
            supplierBookValueInventoryMapper.upsertReceiving(
                    header.getReceivingStore(),
                    detail.getItemCode(),
                    header.getSupplierCode(),
                    header.getSupplierContract(),
                    detail.getReceivingQty()
            );
        }

        csrvMapper.updateHeaderStatus(id, STATUS_RELEASED, Instant.now());
        return getById(id);
    }

    private CsrvResponse persist(CsrvRequest request) {
        CsrvHeaderEntity header = new CsrvHeaderEntity();
        header.setId(UUID.randomUUID().toString());
        header.setDocNo(nextDocNo());
        header.setCompany(request.company());
        header.setReceivingStore(request.receivingStore());
        header.setSupplierCode(request.supplierCode());
        header.setSupplierContract(request.supplierContract());
        header.setBranch(request.branch());
        header.setSupplierDoNo(request.supplierDoNo());
        header.setDeliveryDate(request.deliveryDate());
        header.setRemark(request.remark());
        header.setStatus(STATUS_HELD);
        header.setCreatedBy(request.createdBy());
        header.setCreatedMethod(request.createdMethod());
        header.setReferenceNo(request.referenceNo());
        csrvMapper.insertHeader(header);

        for (CsrvDetailRequest item : request.items()) {
            CsrvDetailEntity detail = new CsrvDetailEntity();
            detail.setId(UUID.randomUUID().toString());
            detail.setCsrvId(header.getId());
            detail.setItemCode(item.itemCode());
            detail.setAvailableQty(item.availableQty());
            detail.setRequestQty(item.requestQty());
            detail.setReceivingQty(item.receivingQty());
            csrvMapper.insertDetail(detail);
        }

        return getById(header.getId());
    }

    private void validateSetupRegistration(CsrvRequest request) {
        for (CsrvDetailRequest item : request.items()) {
            long matches = csrvMapper.countMatchingSetup(
                    item.itemCode(),
                    request.supplierCode(),
                    request.supplierContract(),
                    request.receivingStore()
            );
            if (matches <= 0) {
                throw new BusinessRuleViolationException(
                        "Item " + item.itemCode() + " is not registered for supplier/contract/store in consignment setup"
                );
            }
        }
    }

    private CsrvHeaderEntity loadHeader(String id) {
        CsrvHeaderEntity header = csrvMapper.findHeaderById(id);
        if (header == null) {
            throw new ResourceNotFoundException("CSRV not found: " + id);
        }
        return header;
    }

    private String nextDocNo() {
        return "CSRV-" + String.format("%05d", sequence.getAndIncrement());
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private CsrvResponse toResponse(CsrvHeaderEntity header, List<CsrvDetailEntity> details) {
        List<CsrvResponseDetail> items = details.stream()
                .map(detail -> new CsrvResponseDetail(
                        detail.getId(),
                        detail.getItemCode(),
                        detail.getAvailableQty(),
                        detail.getRequestQty(),
                        detail.getReceivingQty()
                ))
                .toList();

        return new CsrvResponse(
                header.getId(),
                header.getDocNo(),
                header.getCompany(),
                header.getReceivingStore(),
                header.getSupplierCode(),
                header.getSupplierContract(),
                header.getBranch(),
                header.getSupplierDoNo(),
                header.getDeliveryDate(),
                header.getRemark(),
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
