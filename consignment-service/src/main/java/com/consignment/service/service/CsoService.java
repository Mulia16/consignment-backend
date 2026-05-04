package com.consignment.service.service;

import com.consignment.service.constant.ConsignmentConstants;
import com.consignment.service.exception.BusinessRuleViolationException;
import com.consignment.service.exception.RequestValidationException;
import com.consignment.service.exception.ResourceNotFoundException;
import com.consignment.service.model.PageMeta;
import com.consignment.service.model.csdo.CsdoTransferRequest;
import com.consignment.service.model.cso.CsoDetailRequest;
import com.consignment.service.model.cso.CsoRequest;
import com.consignment.service.model.cso.CsoResponse;
import com.consignment.service.model.cso.CsoResponseDetail;
import com.consignment.service.model.cso.CsoSearchCriteria;
import com.consignment.service.model.cso.CsoUpdateRequest;
import com.consignment.service.persistence.mapper.CsoMapper;
import com.consignment.service.persistence.mapper.ReservationMapper;
import com.consignment.service.persistence.model.CsoDetailEntity;
import com.consignment.service.persistence.model.CsoHeaderEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class CsoService {

    private static final Logger log = LoggerFactory.getLogger(CsoService.class);

    private static final String STATUS_HELD = ConsignmentConstants.STATUS_HELD;
    private static final String STATUS_RELEASED = ConsignmentConstants.STATUS_RELEASED;
    private static final String STATUS_ERROR = "ERROR";
    private static final String DOC_TYPE_CSO = "CSO";
    private static final String RESERVATION_ALLOCATE = "Allocate";
    private static final String RESERVATION_FORECAST = "Forecast";
    private static final String METHOD_API = "API";

    private final CsoMapper csoMapper;
    private final ReservationMapper reservationMapper;
    private final CsdoService csdoService;

    public CsoService(CsoMapper csoMapper, ReservationMapper reservationMapper, CsdoService csdoService) {
        this.csoMapper = csoMapper;
        this.reservationMapper = reservationMapper;
        this.csdoService = csdoService;
    }

    @Transactional
    public CsoResponse update(String id, CsoUpdateRequest request) {
        log.info("Updating CSO id={} items={}", id, request.items() == null ? 0 : request.items().size());
        CsoHeaderEntity header = loadHeader(id);
        if (!STATUS_HELD.equalsIgnoreCase(header.getStatus())) {
            throw new BusinessRuleViolationException("Only CSO with status HELD can be updated");
        }
        header.setCustomerBranch(request.customerBranch());
        header.setCustomerEmail(request.customerEmail());
        header.setNote(request.note());
        header.setReferenceNo(request.referenceNo());
        header.setShippingTerm(request.shippingTerm());
        header.setDeliveryDate(request.deliveryDate());
        header.setShippingMode(request.shippingMode());
        header.setTransporter(request.transporter());
        header.setShippingTo(request.shippingTo());
        header.setShippingAddress(request.shippingAddress());
        header.setCustomerReference(request.customerReference());
        header.setTransportInformation(request.transportInformation());
        csoMapper.updateHeader(header);

        csoMapper.deleteDetails(id);
        for (CsoDetailRequest item : request.items()) {
            CsoDetailEntity detail = new CsoDetailEntity();
            detail.setId(UUID.randomUUID().toString());
            detail.setCsoId(id);
            detail.setItemCode(item.itemCode());
            detail.setQty(item.qty());
            detail.setUom(item.uom());
            csoMapper.insertDetail(detail);
        }
        return getById(id);
    }

    @Transactional
    public CsoResponse create(CsoRequest request) {
        log.info("Creating CSO store={} customer={} supplier={}", request.store(), request.customerCode(), request.supplierCode());
        List<Map<String, String>> setupErrors = validateSetup(request);
        if (!setupErrors.isEmpty()) {
            throw new RequestValidationException(
                    "Validation failed for CSO item setup",
                    setupErrors
            );
        }
        CsoHeaderEntity header = persistHeaderAndDetails(request, STATUS_HELD);
        return getById(header.getId());
    }

    @Transactional
    public CsoResponse autoCreate(CsoRequest request) {
        log.info("Auto-creating CSO store={} createdMethod={}", request.store(), request.createdMethod());
        if (!METHOD_API.equalsIgnoreCase(request.createdMethod())) {
            throw new BusinessRuleViolationException("Auto-create CSO requires createdMethod = API");
        }

        boolean validSetup = validateSetup(request).isEmpty();
        String status = validSetup ? STATUS_HELD : STATUS_ERROR;

        CsoHeaderEntity header = persistHeaderAndDetails(request, status);
        if (!validSetup) {
            postReservation(header, csoMapper.findDetailsByHeaderId(header.getId()));
        }

        return getById(header.getId());
    }

    public record PagedResult(List<CsoResponse> items, PageMeta meta) {}

    public PagedResult search(CsoSearchCriteria criteria) {
        List<CsoResponse> items = csoMapper.searchHeaders(criteria).stream()
                .map(h -> toResponse(h, csoMapper.findDetailsByHeaderId(h.getId())))
                .toList();
        long total = csoMapper.countHeaders(criteria);
        return new PagedResult(items, PageMeta.of(criteria.page(), criteria.perPage(), total));
    }

    public CsoResponse getById(String id) {
        CsoHeaderEntity header = loadHeader(id);
        return toResponse(header, csoMapper.findDetailsByHeaderId(id));
    }

    @Transactional
    public CsoResponse release(String id, String releasedBy) {
        log.info("Releasing CSO id={} releasedBy={}", id, releasedBy);
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
        log.info("Deleting CSO id={}", id);
        CsoHeaderEntity header = loadHeader(id);
        if (!STATUS_HELD.equalsIgnoreCase(header.getStatus()) && !STATUS_ERROR.equalsIgnoreCase(header.getStatus())) {
            throw new BusinessRuleViolationException("Only CSO with status HELD or ERROR can be deleted");
        }

        reservationMapper.deleteByDocNo(header.getDocNo());
        csoMapper.deleteHeader(id);
    }

    private CsoHeaderEntity persistHeaderAndDetails(CsoRequest request, String status) {
        CsoHeaderEntity header = null;
        final int maxAttempts = 5;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            header = buildHeader(request, status);
            try {
                csoMapper.insertHeader(header);
                break;
            } catch (DataIntegrityViolationException ex) {
                if (isDocNoConflict(ex) && attempt < maxAttempts) {
                    continue;
                }
                throw ex;
            }
        }

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

    private CsoHeaderEntity buildHeader(CsoRequest request, String status) {
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
        header.setShippingTerm(request.shippingTerm());
        header.setDeliveryDate(request.deliveryDate());
        header.setShippingMode(request.shippingMode());
        header.setTransporter(request.transporter());
        header.setShippingTo(request.shippingTo());
        header.setShippingAddress(request.shippingAddress());
        header.setCustomerReference(request.customerReference());
        header.setTransportInformation(request.transportInformation());
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

    private List<Map<String, String>> validateSetup(CsoRequest request) {
        List<Map<String, String>> errors = new ArrayList<>();
        for (int i = 0; i < request.items().size(); i++) {
            CsoDetailRequest item = request.items().get(i);
            long matches = csoMapper.countMatchingSetup(
                    item.itemCode(),
                    request.supplierCode(),
                    request.supplierContract(),
                    request.store()
            );
            if (matches <= 0) {
                errors.add(Map.of(
                        "field", "items[" + i + "].itemCode",
                        "message", "Item is not registered for supplierCode=" + request.supplierCode()
                                + ", supplierContract=" + request.supplierContract()
                                + ", store=" + request.store()
                ));
            }
        }
        return errors;
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
        Long maxDocNo = csoMapper.findMaxDocNoNumber();
        long nextNumber = (maxDocNo == null ? 0L : maxDocNo) + 1L;
        return "CSO-" + String.format("%05d", nextNumber);
    }

    private boolean isDocNoConflict(DataIntegrityViolationException ex) {
        String message = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
        return message != null && message.contains("cso_header_doc_no_key");
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
                header.getShippingTerm(),
                header.getDeliveryDate(),
                header.getShippingMode(),
                header.getTransporter(),
                header.getShippingTo(),
                header.getShippingAddress(),
                header.getCustomerReference(),
                header.getTransportInformation(),
                header.getReleasedAt(),
                header.getReleasedBy(),
                header.getCreatedAt(),
                header.getUpdatedAt(),
                items
        );
    }
}
