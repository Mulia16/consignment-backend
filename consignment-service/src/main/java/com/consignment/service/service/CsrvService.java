package com.consignment.service.service;

import com.consignment.service.constant.ConsignmentConstants;
import com.consignment.service.exception.BusinessRuleViolationException;
import com.consignment.service.exception.ResourceNotFoundException;
import com.consignment.service.model.PageMeta;
import com.consignment.service.model.csrv.CsrvDetailRequest;
import com.consignment.service.model.csrv.CsrvRequest;
import com.consignment.service.model.csrv.CsrvResponse;
import com.consignment.service.model.csrv.CsrvResponseDetail;
import com.consignment.service.model.csrv.CsrvSearchCriteria;
import com.consignment.service.model.csrv.CsrvUpdateRequest;
import com.consignment.service.persistence.mapper.CsrvMapper;
import com.consignment.service.persistence.mapper.SupplierBookValueInventoryMapper;
import com.consignment.service.persistence.model.CsrvDetailEntity;
import com.consignment.service.persistence.model.CsrvHeaderEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class CsrvService {

    private static final String STATUS_HELD = ConsignmentConstants.STATUS_HELD;
    private static final String STATUS_RELEASED = ConsignmentConstants.STATUS_RELEASED;
    private static final String METHOD_API = "API";

    private final CsrvMapper csrvMapper;
    private final SupplierBookValueInventoryMapper supplierBookValueInventoryMapper;

    public CsrvService(
            CsrvMapper csrvMapper,
            SupplierBookValueInventoryMapper supplierBookValueInventoryMapper
    ) {
        this.csrvMapper = csrvMapper;
        this.supplierBookValueInventoryMapper = supplierBookValueInventoryMapper;
    }

    @Transactional
    public CsrvResponse update(String id, CsrvUpdateRequest request) {
        CsrvHeaderEntity header = loadHeader(id);
        if (!STATUS_HELD.equalsIgnoreCase(header.getStatus())) {
            throw new BusinessRuleViolationException("Only CSRV with status HELD can be updated");
        }
        header.setBranch(request.branch());
        header.setSupplierDoNo(request.supplierDoNo());
        header.setDeliveryDate(request.deliveryDate());
        header.setRemark(request.remark());
        header.setReferenceNo(request.referenceNo());
        csrvMapper.updateHeader(header);

        csrvMapper.deleteDetails(id);
        for (CsrvDetailRequest item : request.items()) {
            CsrvDetailEntity detail = new CsrvDetailEntity();
            detail.setId(UUID.randomUUID().toString());
            detail.setCsrvId(id);
            detail.setItemCode(item.itemCode());
            detail.setAvailableQty(item.availableQty());
            detail.setRequestQty(item.requestQty());
            detail.setReceivingQty(item.receivingQty());
            csrvMapper.insertDetail(detail);
        }
        return getById(id);
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

    public record PagedResult(List<CsrvResponse> items, PageMeta meta) {}

    public PagedResult search(CsrvSearchCriteria criteria) {
        List<CsrvResponse> items = csrvMapper.searchHeaders(criteria).stream()
                .map(h -> toResponse(h, csrvMapper.findDetailsByHeaderId(h.getId())))
                .toList();
        long total = csrvMapper.countHeaders(criteria);
        return new PagedResult(items, PageMeta.of(criteria.page(), criteria.perPage(), total));
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
        Long max = csrvMapper.findMaxDocNoNumber();
        return "CSRV-" + String.format("%05d", (max == null ? 0L : max) + 1L);
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
