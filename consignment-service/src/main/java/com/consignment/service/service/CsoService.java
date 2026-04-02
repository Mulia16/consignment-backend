package com.consignment.service.service;

import com.consignment.service.exception.BusinessRuleViolationException;
import com.consignment.service.exception.ResourceNotFoundException;
import com.consignment.service.model.csdo.CsdoTransferRequest;
import com.consignment.service.model.cso.CsoDetailRequest;
import com.consignment.service.model.cso.CsoRequest;
import com.consignment.service.model.cso.CsoResponse;
import com.consignment.service.model.cso.CsoResponseDetail;
import com.consignment.service.model.cso.CsoSearchCriteria;
import com.consignment.service.persistence.mapper.CsoMapper;
import com.consignment.service.persistence.mapper.ReservationMapper;
import com.consignment.service.persistence.model.CsoDetailEntity;
import com.consignment.service.persistence.model.CsoHeaderEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class CsoService {

    private static final String STATUS_HELD = "HELD";
    private static final String STATUS_RELEASED = "RELEASED";
    private static final String STATUS_ERROR = "ERROR";
    private static final String DOC_TYPE_CSO = "CSO";
    private static final String RESERVATION_ALLOCATE = "Allocate";
    private static final String RESERVATION_FORECAST = "Forecast";
    private static final String METHOD_API = "API";

    private final CsoMapper csoMapper;
    private final ReservationMapper reservationMapper;
    private final CsdoService csdoService;
    private final AtomicLong sequence = new AtomicLong(1);

    public CsoService(CsoMapper csoMapper, ReservationMapper reservationMapper, CsdoService csdoService) {
        this.csoMapper = csoMapper;
        this.reservationMapper = reservationMapper;
        this.csdoService = csdoService;
    }

    @Transactional
    public CsoResponse create(CsoRequest request) {
        if (!isSetupValid(request)) {
            throw new BusinessRuleViolationException("CSO items are not registered for supplier/contract/store in consignment setup");
        }
        CsoHeaderEntity header = persistHeaderAndDetails(request, STATUS_HELD);
        return getById(header.getId());
    }

    @Transactional
    public CsoResponse autoCreate(CsoRequest request) {
        if (!METHOD_API.equalsIgnoreCase(request.createdMethod())) {
            throw new BusinessRuleViolationException("Auto-create CSO requires createdMethod = API");
        }

        boolean validSetup = isSetupValid(request);
        String status = validSetup ? STATUS_HELD : STATUS_ERROR;

        CsoHeaderEntity header = persistHeaderAndDetails(request, status);
        if (!validSetup) {
            postReservation(header, csoMapper.findDetailsByHeaderId(header.getId()));
        }

        return getById(header.getId());
    }

    public List<CsoResponse> search(CsoSearchCriteria criteria) {
        return csoMapper.searchHeaders(
                        normalize(criteria.company()),
                        normalize(criteria.store()),
                        normalize(criteria.customerCode()),
                        normalize(criteria.supplierCode()),
                        normalize(criteria.supplierContract()),
                        normalize(criteria.createdMethod()),
                        normalize(criteria.referenceNo()),
                        normalize(criteria.itemCode()),
                        normalize(criteria.status())
                ).stream()
                .map(header -> toResponse(header, csoMapper.findDetailsByHeaderId(header.getId())))
                .toList();
    }

    public CsoResponse getById(String id) {
        CsoHeaderEntity header = loadHeader(id);
        return toResponse(header, csoMapper.findDetailsByHeaderId(id));
    }

    @Transactional
    public CsoResponse release(String id, String releasedBy) {
        CsoHeaderEntity header = loadHeader(id);
        if (!STATUS_HELD.equalsIgnoreCase(header.getStatus())) {
            throw new BusinessRuleViolationException("Only CSO with status HELD can be released");
        }

        List<CsoDetailEntity> details = csoMapper.findDetailsByHeaderId(id);
        if (!isSetupValid(header, details)) {
            csoMapper.updateHeaderStatus(id, STATUS_ERROR, Instant.now(), normalize(releasedBy));
            postReservation(header, details);
            return getById(id);
        }

        csoMapper.updateHeaderStatus(id, STATUS_RELEASED, Instant.now(), normalize(releasedBy));
        postReservation(header, details);
        if (header.isAutoGenerateCsdo()) {
            csdoService.transferFromCso(id, new CsdoTransferRequest(true, null, null, releasedBy == null ? "system" : releasedBy));
        }
        return getById(id);
    }

    @Transactional
    public void delete(String id) {
        CsoHeaderEntity header = loadHeader(id);
        if (!STATUS_HELD.equalsIgnoreCase(header.getStatus()) && !STATUS_ERROR.equalsIgnoreCase(header.getStatus())) {
            throw new BusinessRuleViolationException("Only CSO with status HELD or ERROR can be deleted");
        }

        reservationMapper.deleteByDocNo(header.getDocNo());
        csoMapper.deleteHeader(id);
    }

    private CsoHeaderEntity persistHeaderAndDetails(CsoRequest request, String status) {
        CsoHeaderEntity header = new CsoHeaderEntity();
        header.setId(UUID.randomUUID().toString());
        header.setDocNo(nextDocNo());
        header.setCompany(request.company());
        header.setStore(request.store());
        header.setCustomerCode(request.customerCode());
        header.setCustomerBranch(request.customerBranch());
        header.setCustomerEmail(request.customerEmail());
        header.setSupplierCode(request.supplierCode());
        header.setSupplierContract(request.supplierContract());
        header.setAutoGenerateCsdo(request.autoGenerateCsdo());
        header.setNote(request.note());
        header.setStatus(status);
        header.setCreatedBy(request.createdBy());
        header.setCreatedMethod(request.createdMethod());
        header.setReferenceNo(request.referenceNo());
        csoMapper.insertHeader(header);

        for (CsoDetailRequest item : request.items()) {
            CsoDetailEntity detail = new CsoDetailEntity();
            detail.setId(UUID.randomUUID().toString());
            detail.setCsoId(header.getId());
            detail.setItemCode(item.itemCode());
            detail.setQty(item.qty());
            detail.setUom(item.uom());
            csoMapper.insertDetail(detail);
        }

        return header;
    }

    private void postReservation(CsoHeaderEntity header, List<CsoDetailEntity> details) {
        reservationMapper.deleteByDocNo(header.getDocNo());

        for (CsoDetailEntity detail : details) {
            reservationMapper.insertReservation(
                    header.getDocNo(),
                    DOC_TYPE_CSO,
                    header.getCustomerBranch() == null || header.getCustomerBranch().isBlank() ? header.getStore() : header.getCustomerBranch(),
                    detail.getItemCode(),
                    detail.getQty(),
                    RESERVATION_ALLOCATE
            );
            reservationMapper.insertReservation(
                    header.getDocNo(),
                    DOC_TYPE_CSO,
                    header.getStore(),
                    detail.getItemCode(),
                    detail.getQty(),
                    RESERVATION_FORECAST
            );
        }
    }

    private boolean isSetupValid(CsoRequest request) {
        for (CsoDetailRequest item : request.items()) {
            long matches = csoMapper.countMatchingSetup(
                    item.itemCode(),
                    request.supplierCode(),
                    request.supplierContract(),
                    request.store()
            );
            if (matches <= 0) {
                return false;
            }
        }
        return true;
    }

    private boolean isSetupValid(CsoHeaderEntity header, List<CsoDetailEntity> details) {
        for (CsoDetailEntity detail : details) {
            long matches = csoMapper.countMatchingSetup(
                    detail.getItemCode(),
                    header.getSupplierCode(),
                    header.getSupplierContract(),
                    header.getStore()
            );
            if (matches <= 0) {
                return false;
            }
        }
        return true;
    }

    private CsoHeaderEntity loadHeader(String id) {
        CsoHeaderEntity header = csoMapper.findHeaderById(id);
        if (header == null) {
            throw new ResourceNotFoundException("CSO not found: " + id);
        }
        return header;
    }

    private String nextDocNo() {
        return "CSO-" + String.format("%05d", sequence.getAndIncrement());
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private CsoResponse toResponse(CsoHeaderEntity header, List<CsoDetailEntity> details) {
        List<CsoResponseDetail> items = details.stream()
                .map(detail -> new CsoResponseDetail(
                        detail.getId(),
                        detail.getItemCode(),
                        detail.getQty(),
                        detail.getUom()
                ))
                .toList();

        return new CsoResponse(
                header.getId(),
                header.getDocNo(),
                header.getCompany(),
                header.getStore(),
                header.getCustomerCode(),
                header.getCustomerBranch(),
                header.getCustomerEmail(),
                header.getSupplierCode(),
                header.getSupplierContract(),
                header.isAutoGenerateCsdo(),
                header.getNote(),
                header.getStatus(),
                header.getCreatedBy(),
                header.getCreatedMethod(),
                header.getReferenceNo(),
                header.getReleasedAt(),
                header.getReleasedBy(),
                header.getCreatedAt(),
                header.getUpdatedAt(),
                items
        );
    }
}
