package com.consignment.service.service;

import com.consignment.service.exception.BusinessRuleViolationException;
import com.consignment.service.exception.ResourceNotFoundException;
import com.consignment.service.model.settlement.SettlementBatchGenerateRequest;
import com.consignment.service.model.settlement.SettlementDocumentPostRequest;
import com.consignment.service.model.settlement.SettlementDocumentSourceRequest;
import com.consignment.service.model.settlement.SettlementDetailResponse;
import com.consignment.service.model.settlement.SettlementRequest;
import com.consignment.service.model.settlement.SettlementResponse;
import com.consignment.service.persistence.mapper.CsaMapper;
import com.consignment.service.persistence.mapper.CsoMapper;
import com.consignment.service.persistence.mapper.CsrMapper;
import com.consignment.service.persistence.mapper.CsrvMapper;
import com.consignment.service.persistence.mapper.CsdoMapper;
import com.consignment.service.persistence.mapper.SettlementRequestMapper;
import com.consignment.service.persistence.model.CsaDetailEntity;
import com.consignment.service.persistence.model.CsaHeaderEntity;
import com.consignment.service.persistence.model.CsoDetailEntity;
import com.consignment.service.persistence.model.CsoHeaderEntity;
import com.consignment.service.persistence.model.CsrDetailEntity;
import com.consignment.service.persistence.model.CsrHeaderEntity;
import com.consignment.service.persistence.model.CsrvDetailEntity;
import com.consignment.service.persistence.model.CsrvHeaderEntity;
import com.consignment.service.persistence.model.CsdoDetailEntity;
import com.consignment.service.persistence.model.CsdoHeaderEntity;
import com.consignment.service.persistence.model.SettlementDetailEntity;
import com.consignment.service.persistence.model.SettlementRequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class SettlementService {

    private static final String STATUS_HELD = "HELD";
    private static final String STATUS_READY_FOR_BILLING = "READY_FOR_BILLING";
    private static final String STATUS_BILLED = "BILLED";
    private static final String STATUS_SETTLED = "SETTLED";
    private static final String STATUS_RELEASED = "RELEASED";
    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String TYPE_CUSTOMER = "CUSTOMER";
    private static final String TYPE_SUPPLIER = "SUPPLIER";
    private static final String DOC_TYPE_CSO = "CSO";
    private static final String DOC_TYPE_CSDO = "CSDO";
    private static final String DOC_TYPE_CSRV = "CSRV";
    private static final String DOC_TYPE_CSR = "CSR";
    private static final String DOC_TYPE_CSA = "CSA";

    private final SettlementRequestMapper settlementRequestMapper;
    private final CsoMapper csoMapper;
    private final CsdoMapper csdoMapper;
    private final CsrvMapper csrvMapper;
    private final CsrMapper csrMapper;
    private final CsaMapper csaMapper;
    private final PricingService pricingService;
    private final AtomicLong sequence = new AtomicLong(1);

    public SettlementService(
            SettlementRequestMapper settlementRequestMapper,
            CsoMapper csoMapper,
            CsdoMapper csdoMapper,
            CsrvMapper csrvMapper,
            CsrMapper csrMapper,
                CsaMapper csaMapper,
                PricingService pricingService
    ) {
        this.settlementRequestMapper = settlementRequestMapper;
        this.csoMapper = csoMapper;
        this.csdoMapper = csdoMapper;
        this.csrvMapper = csrvMapper;
        this.csrMapper = csrMapper;
        this.csaMapper = csaMapper;
        this.pricingService = pricingService;
    }

    @Transactional
    public SettlementResponse create(SettlementRequest request) {
        if (!TYPE_CUSTOMER.equalsIgnoreCase(request.settlementType()) && 
            !TYPE_SUPPLIER.equalsIgnoreCase(request.settlementType())) {
            throw new BusinessRuleViolationException("Settlement type must be CUSTOMER or SUPPLIER");
        }

        if (TYPE_CUSTOMER.equalsIgnoreCase(request.settlementType())) {
            if (request.customerCode() == null || request.customerCode().isBlank()) {
                throw new BusinessRuleViolationException("Customer code is required for CUSTOMER settlement type");
            }
        } else {
            if (request.supplierCode() == null || request.supplierCode().isBlank()) {
                throw new BusinessRuleViolationException("Supplier code is required for SUPPLIER settlement type");
            }
        }

        SettlementRequestEntity header = new SettlementRequestEntity();
        header.setId(UUID.randomUUID().toString());
        header.setDocNo(nextDocNo());
        header.setCompany(request.company());
        header.setStore(request.store());
        header.setSettlementType(request.settlementType());
        header.setCustomerCode(request.customerCode());
        header.setSupplierCode(request.supplierCode());
        header.setSupplierContract(request.supplierContract());
        header.setTotalAmount(BigDecimal.ZERO);
        header.setCurrency(request.currency());
        header.setStatus(STATUS_HELD);
        header.setCreatedBy(request.createdBy());
        header.setReferenceNo(request.referenceNo());
        header.setRemark(request.remark());
        settlementRequestMapper.insertHeader(header);

        return getById(header.getId());
    }

    @Transactional
    public SettlementResponse generateBatch(SettlementBatchGenerateRequest request) {
        validatePeriod(request.fromDate(), request.toDate());

        SettlementResponse created = create(new SettlementRequest(
                request.company(),
                request.store(),
                request.settlementType(),
                request.customerCode(),
                request.supplierCode(),
                request.supplierContract(),
                request.currency(),
            request.createdBy(),
            request.referenceNo(),
            "Batch generated for period " + request.fromDate() + " to " + request.toDate()
        ));

        if (request.referenceNo() != null && !request.referenceNo().isBlank()) {
            if (settlementRequestMapper.countOpenByReference(
                request.company(),
                request.store(),
                request.settlementType(),
                request.referenceNo()
            ) > 1) {
            throw new BusinessRuleViolationException("Batch with same referenceNo already exists and is not settled: " + request.referenceNo());
            }
        }

        List<SettlementDocumentSourceRequest> sources = collectBatchSources(request);
        if (sources.isEmpty()) {
            throw new BusinessRuleViolationException("No eligible source documents found for the requested period");
        }

        return postDetailsFromDocuments(created.id(), new SettlementDocumentPostRequest(sources));
    }

    public List<SettlementResponse> listAll() {
        return settlementRequestMapper.findAllHeaders().stream()
                .map(header -> toResponse(header, settlementRequestMapper.findDetailsByHeaderId(header.getId())))
                .toList();
    }

    public List<SettlementResponse> search(String company, String store, String settlementType, 
                                           String customerCode, String supplierCode, String status, String createdBy) {
        return settlementRequestMapper.searchHeaders(
                        normalize(company),
                        normalize(store),
                        normalize(settlementType),
                        normalize(customerCode),
                        normalize(supplierCode),
                        normalize(status),
                        normalize(createdBy)
                ).stream()
                .map(header -> toResponse(header, settlementRequestMapper.findDetailsByHeaderId(header.getId())))
                .toList();
    }

    public SettlementResponse getById(String id) {
        SettlementRequestEntity header = loadHeader(id);
        return toResponse(header, settlementRequestMapper.findDetailsByHeaderId(id));
    }

    @Transactional
    public SettlementResponse postDetailsFromDocuments(String id, SettlementDocumentPostRequest request) {
        SettlementRequestEntity header = loadHeader(id);
        if (!STATUS_HELD.equalsIgnoreCase(header.getStatus())) {
            throw new BusinessRuleViolationException("Settlement details can only be posted when status is HELD");
        }

        for (SettlementDocumentSourceRequest document : request.documents()) {
            postDocument(header, document);
        }

        refreshTotalAmount(id);
        return getById(id);
    }

    @Transactional
    public SettlementResponse prepareForBilling(String id) {
        SettlementRequestEntity header = loadHeader(id);
        if (!STATUS_HELD.equalsIgnoreCase(header.getStatus())) {
            throw new BusinessRuleViolationException("Only HELD settlement requests can be prepared for billing");
        }

        List<SettlementDetailEntity> details = settlementRequestMapper.findDetailsByHeaderId(id);
        if (details.isEmpty()) {
            throw new BusinessRuleViolationException("Settlement request must have at least one detail line");
        }

        BigDecimal totalAmount = details.stream()
                .map(detail -> detail.getLineAmount() == null ? BigDecimal.ZERO : detail.getLineAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        settlementRequestMapper.updateTotalAmount(id, totalAmount);
        settlementRequestMapper.updateHeaderStatus(id, STATUS_READY_FOR_BILLING, Instant.now(), null, null);
        return getById(id);
    }

    @Transactional
    public SettlementResponse markAsBilled(String id) {
        SettlementRequestEntity header = loadHeader(id);
        if (!STATUS_READY_FOR_BILLING.equalsIgnoreCase(header.getStatus())) {
            throw new BusinessRuleViolationException("Only READY_FOR_BILLING settlement requests can be marked as billed");
        }

        settlementRequestMapper.updateHeaderStatus(id, STATUS_BILLED, null, Instant.now(), null);
        return getById(id);
    }

    @Transactional
    public SettlementResponse markAsSettled(String id) {
        SettlementRequestEntity header = loadHeader(id);
        if (!STATUS_BILLED.equalsIgnoreCase(header.getStatus())) {
            throw new BusinessRuleViolationException("Only BILLED settlement requests can be marked as settled");
        }

        settlementRequestMapper.updateHeaderStatus(id, STATUS_SETTLED, null, null, Instant.now());
        return getById(id);
    }

    private String nextDocNo() {
        return "SETTL-" + String.format("%05d", sequence.getAndIncrement());
    }

    private SettlementRequestEntity loadHeader(String id) {
        SettlementRequestEntity header = settlementRequestMapper.findHeaderById(id);
        if (header == null) {
            throw new ResourceNotFoundException("Settlement request not found: " + id);
        }
        return header;
    }

    private void refreshTotalAmount(String id) {
        BigDecimal totalAmount = settlementRequestMapper.findDetailsByHeaderId(id).stream()
                .map(detail -> detail.getLineAmount() == null ? BigDecimal.ZERO : detail.getLineAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        settlementRequestMapper.updateTotalAmount(id, totalAmount);
    }

    private void postDocument(SettlementRequestEntity settlement, SettlementDocumentSourceRequest document) {
        String documentType = document.documentType().trim().toUpperCase();

        switch (documentType) {
            case DOC_TYPE_CSO -> postCsoDetails(settlement, document);
            case DOC_TYPE_CSDO -> postCsdoDetails(settlement, document);
            case DOC_TYPE_CSRV -> postCsrvDetails(settlement, document);
            case DOC_TYPE_CSR -> postCsrDetails(settlement, document);
            case DOC_TYPE_CSA -> postCsaDetails(settlement, document);
            default -> throw new BusinessRuleViolationException("Unsupported settlement document type: " + document.documentType());
        }
    }

    private List<SettlementDocumentSourceRequest> collectBatchSources(SettlementBatchGenerateRequest request) {
        BigDecimal defaultUnitPrice = request.defaultUnitPrice();
        List<SettlementDocumentSourceRequest> sources = new ArrayList<>();

        if (TYPE_CUSTOMER.equalsIgnoreCase(request.settlementType())) {
            List<CsoHeaderEntity> csoHeaders = csoMapper.searchHeaders(
                    request.company(),
                    request.store(),
                    request.customerCode(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    STATUS_RELEASED
            );
            for (CsoHeaderEntity header : csoHeaders) {
                if (isWithinPeriod(header.getCreatedAt(), request.fromDate(), request.toDate())) {
                    sources.add(new SettlementDocumentSourceRequest(DOC_TYPE_CSO, header.getId(), defaultUnitPrice, "batch-generated"));
                }
            }

            List<CsdoHeaderEntity> csdoHeaders = csdoMapper.searchHeaders(
                    request.company(),
                    request.store(),
                    request.customerCode(),
                    STATUS_RELEASED,
                    null,
                    null,
                    null
            );
            for (CsdoHeaderEntity header : csdoHeaders) {
                if (isWithinPeriod(header.getCreatedAt(), request.fromDate(), request.toDate())) {
                    sources.add(new SettlementDocumentSourceRequest(DOC_TYPE_CSDO, header.getId(), defaultUnitPrice, "batch-generated"));
                }
            }

            return sources;
        }

        List<CsrvHeaderEntity> csrvHeaders = csrvMapper.searchHeaders(
                request.company(),
                request.store(),
                request.supplierCode(),
                request.supplierContract(),
                null,
                null,
                null,
                null,
                STATUS_RELEASED
        );
        for (CsrvHeaderEntity header : csrvHeaders) {
            if (isWithinPeriod(header.getCreatedAt(), request.fromDate(), request.toDate())) {
                sources.add(new SettlementDocumentSourceRequest(DOC_TYPE_CSRV, header.getId(), defaultUnitPrice, "batch-generated"));
            }
        }

        List<CsrHeaderEntity> csrHeaders = csrMapper.searchHeaders(
                request.company(),
                request.store(),
                request.supplierCode(),
                request.supplierContract(),
                STATUS_COMPLETED,
                null,
                null,
                null
        );
        for (CsrHeaderEntity header : csrHeaders) {
            if (isWithinPeriod(header.getCreatedAt(), request.fromDate(), request.toDate())) {
                sources.add(new SettlementDocumentSourceRequest(DOC_TYPE_CSR, header.getId(), defaultUnitPrice, "batch-generated"));
            }
        }

        List<CsaHeaderEntity> csaHeaders = csaMapper.searchHeaders(
                request.company(),
                request.store(),
                null,
                STATUS_RELEASED,
                null,
                null
        );
        for (CsaHeaderEntity header : csaHeaders) {
            boolean supplierMatch = header.getSupplierCode() != null
                    && header.getSupplierCode().equals(request.supplierCode());
            boolean contractMatch = request.supplierContract() == null
                    || request.supplierContract().isBlank()
                    || request.supplierContract().equals(header.getSupplierContract());
            if (supplierMatch && contractMatch && isWithinPeriod(header.getCreatedAt(), request.fromDate(), request.toDate())) {
                sources.add(new SettlementDocumentSourceRequest(DOC_TYPE_CSA, header.getId(), defaultUnitPrice, "batch-generated"));
            }
        }

        return sources;
    }

    private boolean isWithinPeriod(Instant timestamp, LocalDate fromDate, LocalDate toDate) {
        if (timestamp == null) {
            return false;
        }

        LocalDate value = timestamp.atZone(ZoneOffset.UTC).toLocalDate();
        if (fromDate != null && value.isBefore(fromDate)) {
            return false;
        }
        if (toDate != null && value.isAfter(toDate)) {
            return false;
        }
        return true;
    }

    private void validatePeriod(LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new BusinessRuleViolationException("fromDate must be earlier than or equal to toDate");
        }
    }

    private void postCsoDetails(SettlementRequestEntity settlement, SettlementDocumentSourceRequest document) {
        requireSettlementType(settlement, TYPE_CUSTOMER, DOC_TYPE_CSO);
        CsoHeaderEntity header = csoMapper.findHeaderById(document.documentId());
        if (header == null) {
            throw new ResourceNotFoundException("CSO not found: " + document.documentId());
        }
        requireDocumentStatus(DOC_TYPE_CSO, header.getDocNo(), header.getStatus(), STATUS_RELEASED);
        validateCustomerAlignment(settlement, header.getCompany(), header.getStore(), header.getCustomerCode(), DOC_TYPE_CSO, header.getDocNo());

        for (CsoDetailEntity detail : csoMapper.findDetailsByHeaderId(header.getId())) {
            insertDetailIfAbsent(
                    settlement.getId(),
                    DOC_TYPE_CSO,
                    header.getDocNo(),
                    detail.getItemCode(),
                    detail.getQty(),
                    detail.getUom(),
                    document.unitPrice(),
                    document.remark(),
                    header.getCompany(),
                    header.getStore(),
                    header.getSupplierCode(),
                    header.getSupplierContract(),
                    header.getCustomerCode()
            );
        }
    }

    private void postCsdoDetails(SettlementRequestEntity settlement, SettlementDocumentSourceRequest document) {
        requireSettlementType(settlement, TYPE_CUSTOMER, DOC_TYPE_CSDO);
        CsdoHeaderEntity header = csdoMapper.findHeaderById(document.documentId());
        if (header == null) {
            throw new ResourceNotFoundException("CSDO not found: " + document.documentId());
        }
        requireDocumentStatus(DOC_TYPE_CSDO, header.getDocNo(), header.getStatus(), STATUS_RELEASED);
        validateCustomerAlignment(settlement, header.getCompany(), header.getStore(), header.getCustomerCode(), DOC_TYPE_CSDO, header.getDocNo());

        for (CsdoDetailEntity detail : csdoMapper.findDetailsByHeaderId(header.getId())) {
            insertDetailIfAbsent(
                    settlement.getId(),
                    DOC_TYPE_CSDO,
                    header.getDocNo(),
                    detail.getItemCode(),
                    detail.getQty(),
                    detail.getUom(),
                    document.unitPrice(),
                    document.remark(),
                    header.getCompany(),
                    header.getStore(),
                    settlement.getSupplierCode(),
                    settlement.getSupplierContract(),
                    header.getCustomerCode()
            );
        }
    }

    private void postCsrvDetails(SettlementRequestEntity settlement, SettlementDocumentSourceRequest document) {
        requireSettlementType(settlement, TYPE_SUPPLIER, DOC_TYPE_CSRV);
        CsrvHeaderEntity header = csrvMapper.findHeaderById(document.documentId());
        if (header == null) {
            throw new ResourceNotFoundException("CSRV not found: " + document.documentId());
        }
        requireDocumentStatus(DOC_TYPE_CSRV, header.getDocNo(), header.getStatus(), STATUS_RELEASED);
        validateSupplierAlignment(
                settlement,
                header.getCompany(),
                header.getReceivingStore(),
                header.getSupplierCode(),
                header.getSupplierContract(),
                DOC_TYPE_CSRV,
                header.getDocNo()
        );

        for (CsrvDetailEntity detail : csrvMapper.findDetailsByHeaderId(header.getId())) {
            insertDetailIfAbsent(
                    settlement.getId(),
                    DOC_TYPE_CSRV,
                    header.getDocNo(),
                    detail.getItemCode(),
                    detail.getReceivingQty(),
                    null,
                    document.unitPrice(),
                    document.remark(),
                    header.getCompany(),
                    header.getReceivingStore(),
                    header.getSupplierCode(),
                    header.getSupplierContract(),
                    settlement.getCustomerCode()
            );
        }
    }

    private void postCsrDetails(SettlementRequestEntity settlement, SettlementDocumentSourceRequest document) {
        requireSettlementType(settlement, TYPE_SUPPLIER, DOC_TYPE_CSR);
        CsrHeaderEntity header = csrMapper.findHeaderById(document.documentId());
        if (header == null) {
            throw new ResourceNotFoundException("CSR not found: " + document.documentId());
        }
        requireDocumentStatus(DOC_TYPE_CSR, header.getDocNo(), header.getStatus(), STATUS_COMPLETED);
        validateSupplierAlignment(
                settlement,
                header.getCompany(),
                header.getStore(),
                header.getSupplierCode(),
                header.getSupplierContract(),
                DOC_TYPE_CSR,
                header.getDocNo()
        );

        for (CsrDetailEntity detail : csrMapper.findDetailsByHeaderId(header.getId())) {
            BigDecimal actualQty = detail.getActualQty() == null ? detail.getQty() : detail.getActualQty();
            insertDetailIfAbsent(
                    settlement.getId(),
                    DOC_TYPE_CSR,
                    header.getDocNo(),
                    detail.getItemCode(),
                    actualQty.negate(),
                    detail.getUom(),
                    document.unitPrice(),
                    document.remark(),
                    header.getCompany(),
                    header.getStore(),
                    header.getSupplierCode(),
                    header.getSupplierContract(),
                    settlement.getCustomerCode()
            );
        }
    }

    private void postCsaDetails(SettlementRequestEntity settlement, SettlementDocumentSourceRequest document) {
        requireSettlementType(settlement, TYPE_SUPPLIER, DOC_TYPE_CSA);
        CsaHeaderEntity header = csaMapper.findHeaderById(document.documentId());
        if (header == null) {
            throw new ResourceNotFoundException("CSA not found: " + document.documentId());
        }
        requireDocumentStatus(DOC_TYPE_CSA, header.getDocNo(), header.getStatus(), STATUS_RELEASED);
        validateSupplierAlignment(
                settlement,
                header.getCompany(),
                header.getStore(),
                header.getSupplierCode(),
                header.getSupplierContract(),
                DOC_TYPE_CSA,
                header.getDocNo()
        );

        for (CsaDetailEntity detail : csaMapper.findDetailsByHeaderId(header.getId())) {
            BigDecimal signedQty = "ADJ_OUT".equalsIgnoreCase(header.getTransactionType())
                    ? detail.getQty().negate()
                    : detail.getQty();
            insertDetailIfAbsent(
                    settlement.getId(),
                    DOC_TYPE_CSA,
                    header.getDocNo(),
                    detail.getItemCode(),
                    signedQty,
                    detail.getUom(),
                    document.unitPrice(),
                    document.remark(),
                    header.getCompany(),
                    header.getStore(),
                    header.getSupplierCode(),
                    header.getSupplierContract(),
                    settlement.getCustomerCode()
            );
        }
    }

    private void insertDetailIfAbsent(
            String settlementId,
            String documentType,
            String documentNo,
            String itemCode,
            BigDecimal qty,
            String uom,
            BigDecimal unitPrice,
                String remark,
                String company,
                String store,
                String supplierCode,
                String supplierContract,
                String customerCode
    ) {
        if (settlementRequestMapper.countExistingDetail(settlementId, documentType, documentNo, itemCode) > 0) {
            return;
        }

        BigDecimal effectiveUnitPrice = unitPrice == null
            ? pricingService.resolveUnitPrice(itemCode, company, store, supplierCode, supplierContract, customerCode)
            : unitPrice;
        SettlementDetailEntity detail = new SettlementDetailEntity();
        detail.setId(UUID.randomUUID().toString());
        detail.setSettlementId(settlementId);
        detail.setDocumentType(documentType);
        detail.setDocumentNo(documentNo);
        detail.setItemCode(itemCode);
        detail.setQty(qty);
        detail.setUom(uom);
        detail.setUnitPrice(effectiveUnitPrice);
        detail.setLineAmount(qty.multiply(effectiveUnitPrice));
        detail.setRemark(normalize(remark));
        settlementRequestMapper.insertDetail(detail);
    }

    private void requireSettlementType(SettlementRequestEntity settlement, String expectedType, String documentType) {
        if (!expectedType.equalsIgnoreCase(settlement.getSettlementType())) {
            throw new BusinessRuleViolationException(documentType + " can only be posted into " + expectedType + " settlements");
        }
    }

    private void requireDocumentStatus(String documentType, String documentNo, String actualStatus, String expectedStatus) {
        if (!expectedStatus.equalsIgnoreCase(actualStatus)) {
            throw new BusinessRuleViolationException(
                    documentType + " " + documentNo + " must be in status " + expectedStatus + " before settlement posting"
            );
        }
    }

    private void validateCustomerAlignment(
            SettlementRequestEntity settlement,
            String company,
            String store,
            String customerCode,
            String documentType,
            String documentNo
    ) {
        if (!settlement.getCompany().equals(company) || !settlement.getStore().equals(store)) {
            throw new BusinessRuleViolationException(documentType + " " + documentNo + " does not match settlement company/store");
        }
        if (!settlement.getCustomerCode().equals(customerCode)) {
            throw new BusinessRuleViolationException(documentType + " " + documentNo + " does not match settlement customer");
        }
    }

    private void validateSupplierAlignment(
            SettlementRequestEntity settlement,
            String company,
            String store,
            String supplierCode,
            String supplierContract,
            String documentType,
            String documentNo
    ) {
        if (!settlement.getCompany().equals(company) || !settlement.getStore().equals(store)) {
            throw new BusinessRuleViolationException(documentType + " " + documentNo + " does not match settlement company/store");
        }
        if (!settlement.getSupplierCode().equals(supplierCode)) {
            throw new BusinessRuleViolationException(documentType + " " + documentNo + " does not match settlement supplier");
        }
        if (settlement.getSupplierContract() != null
                && !settlement.getSupplierContract().isBlank()
                && !settlement.getSupplierContract().equals(supplierContract)) {
            throw new BusinessRuleViolationException(documentType + " " + documentNo + " does not match settlement contract");
        }
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private SettlementResponse toResponse(SettlementRequestEntity header, List<SettlementDetailEntity> details) {
        List<SettlementDetailResponse> items = details.stream()
                .map(detail -> new SettlementDetailResponse(
                        detail.getId(),
                        detail.getDocumentType(),
                        detail.getDocumentNo(),
                        detail.getItemCode(),
                        detail.getQty(),
                        detail.getUom(),
                        detail.getUnitPrice(),
                        detail.getLineAmount(),
                        detail.getRemark(),
                        detail.getCreatedAt()
                ))
                .toList();

        return new SettlementResponse(
                header.getId(),
                header.getDocNo(),
                header.getCompany(),
                header.getStore(),
                header.getSettlementType(),
                header.getCustomerCode(),
                header.getSupplierCode(),
                header.getSupplierContract(),
                header.getTotalAmount(),
                header.getCurrency(),
                header.getStatus(),
                header.getCreatedBy(),
                header.getReferenceNo(),
                header.getRemark(),
                header.getReadyForBillingAt(),
                header.getBilledAt(),
                header.getSettledAt(),
                header.getCreatedAt(),
                header.getUpdatedAt(),
                items
        );
    }
}
