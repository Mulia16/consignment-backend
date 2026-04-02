package com.consignment.service.service;

import com.consignment.service.exception.BusinessRuleViolationException;
import com.consignment.service.exception.ResourceNotFoundException;
import com.consignment.service.model.csdo.CsdoResponse;
import com.consignment.service.model.csdo.CsdoResponseDetail;
import com.consignment.service.model.csdo.CsdoSearchCriteria;
import com.consignment.service.model.csdo.CsdoTransferRequest;
import com.consignment.service.persistence.mapper.CsoMapper;
import com.consignment.service.persistence.mapper.CsdoMapper;
import com.consignment.service.persistence.mapper.InventoryMutationMapper;
import com.consignment.service.persistence.mapper.ReservationMapper;
import com.consignment.service.persistence.model.CsoDetailEntity;
import com.consignment.service.persistence.model.CsoHeaderEntity;
import com.consignment.service.persistence.model.CsdoDetailEntity;
import com.consignment.service.persistence.model.CsdoHeaderEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class CsdoService {

    private static final String STATUS_HELD = "HELD";
    private static final String STATUS_RELEASED = "RELEASED";
    private static final String STATUS_REVERSED = "REVERSED";
    private static final String CSO_STATUS_RELEASED = "RELEASED";
    private static final String DOC_TYPE_CSDO = "CSDO";
    private static final String RESERVATION_ALLOCATE = "Allocate";
    private static final String RESERVATION_FORECAST = "Forecast";

    private final CsdoMapper csdoMapper;
    private final CsoMapper csoMapper;
    private final ReservationMapper reservationMapper;
    private final InventoryMutationMapper inventoryMutationMapper;
    private final AtomicLong sequence = new AtomicLong(1);

    public CsdoService(
            CsdoMapper csdoMapper,
            CsoMapper csoMapper,
            ReservationMapper reservationMapper,
            InventoryMutationMapper inventoryMutationMapper
    ) {
        this.csdoMapper = csdoMapper;
        this.csoMapper = csoMapper;
        this.reservationMapper = reservationMapper;
        this.inventoryMutationMapper = inventoryMutationMapper;
    }

    @Transactional
    public CsdoResponse transferFromCso(String csoId, CsdoTransferRequest request) {
        CsoHeaderEntity csoHeader = csoMapper.findHeaderById(csoId);
        if (csoHeader == null) {
            throw new ResourceNotFoundException("CSO not found: " + csoId);
        }
        if (!CSO_STATUS_RELEASED.equalsIgnoreCase(csoHeader.getStatus())) {
            throw new BusinessRuleViolationException("CSDO can only be transferred from CSO with status RELEASED");
        }

        CsdoHeaderEntity existing = csdoMapper.findByCsoId(csoId);
        if (existing != null) {
            return getById(existing.getId());
        }

        CsdoHeaderEntity csdoHeader = new CsdoHeaderEntity();
        csdoHeader.setId(UUID.randomUUID().toString());
        csdoHeader.setDocNo(nextDocNo());
        csdoHeader.setCsoId(csoHeader.getId());
        csdoHeader.setCsoDocNo(csoHeader.getDocNo());
        csdoHeader.setCompany(csoHeader.getCompany());
        csdoHeader.setStore(csoHeader.getStore());
        csdoHeader.setCustomerCode(csoHeader.getCustomerCode());
        csdoHeader.setCustomerBranch(csoHeader.getCustomerBranch());
        csdoHeader.setCustomerEmail(csoHeader.getCustomerEmail());
        csdoHeader.setRequireGenerateCdo(request.requireGenerateCdo());
        csdoHeader.setShippingMode(request.shippingMode());
        csdoHeader.setTransporter(request.transporter());
        csdoHeader.setStatus(STATUS_HELD);
        csdoHeader.setCreatedBy(request.createdBy());
        csdoMapper.insertHeader(csdoHeader);

        List<CsoDetailEntity> csoDetails = csoMapper.findDetailsByHeaderId(csoId);
        for (CsoDetailEntity csoDetail : csoDetails) {
            CsdoDetailEntity csdoDetail = new CsdoDetailEntity();
            csdoDetail.setId(UUID.randomUUID().toString());
            csdoDetail.setCsdoId(csdoHeader.getId());
            csdoDetail.setItemCode(csoDetail.getItemCode());
            csdoDetail.setQty(csoDetail.getQty());
            csdoDetail.setUom(csoDetail.getUom());
            csdoMapper.insertDetail(csdoDetail);
        }

        return getById(csdoHeader.getId());
    }

    public List<CsdoResponse> listAll() {
        return csdoMapper.findAllHeaders().stream()
                .map(header -> toResponse(header, csdoMapper.findDetailsByHeaderId(header.getId())))
                .toList();
    }

    public List<CsdoResponse> search(CsdoSearchCriteria criteria) {
        return csdoMapper.searchHeaders(
                        normalize(criteria.company()),
                        normalize(criteria.store()),
                        normalize(criteria.customerCode()),
                        normalize(criteria.status()),
                        normalize(criteria.createdMethod()),
                        normalize(criteria.referenceNo()),
                        normalize(criteria.itemCode())
                ).stream()
                .map(header -> toResponse(header, csdoMapper.findDetailsByHeaderId(header.getId())))
                .toList();
    }

    public CsdoResponse getById(String id) {
        CsdoHeaderEntity header = csdoMapper.findHeaderById(id);
        if (header == null) {
            throw new ResourceNotFoundException("CSDO not found: " + id);
        }
        return toResponse(header, csdoMapper.findDetailsByHeaderId(id));
    }

    @Transactional
    public CsdoResponse release(String id) {
        CsdoHeaderEntity header = csdoMapper.findHeaderById(id);
        if (header == null) {
            throw new ResourceNotFoundException("CSDO not found: " + id);
        }
        if (!STATUS_HELD.equalsIgnoreCase(header.getStatus())) {
            throw new BusinessRuleViolationException("Only CSDO with status HELD can be released");
        }

        List<CsdoDetailEntity> details = csdoMapper.findDetailsByHeaderId(id);
        reservationMapper.deleteByDocNo(header.getCsoDocNo());

        if (header.isRequireGenerateCdo()) {
            for (CsdoDetailEntity detail : details) {
                reservationMapper.insertReservation(
                        header.getDocNo(),
                        DOC_TYPE_CSDO,
                        header.getCustomerBranch() == null || header.getCustomerBranch().isBlank() ? header.getStore() : header.getCustomerBranch(),
                        detail.getItemCode(),
                        detail.getQty(),
                        RESERVATION_ALLOCATE
                );
                reservationMapper.insertReservation(
                        header.getDocNo(),
                        DOC_TYPE_CSDO,
                        header.getStore(),
                        detail.getItemCode(),
                        detail.getQty(),
                        RESERVATION_FORECAST
                );
            }
        } else {
            for (CsdoDetailEntity detail : details) {
                inventoryMutationMapper.upsertCustomerInventory(
                        header.getStore(),
                        header.getCustomerCode(),
                        header.getCustomerBranch(),
                        detail.getItemCode(),
                        detail.getQty()
                );
            }
        }

        csdoMapper.updateHeaderStatus(id, STATUS_RELEASED, Instant.now());
        return getById(id);
    }

    @Transactional
    public CsdoResponse reverseCorrection(String id) {
        CsdoHeaderEntity header = csdoMapper.findHeaderById(id);
        if (header == null) {
            throw new ResourceNotFoundException("CSDO not found: " + id);
        }
        if (!STATUS_RELEASED.equalsIgnoreCase(header.getStatus())) {
            throw new BusinessRuleViolationException("Only released CSDO can be reversed");
        }

        List<CsdoDetailEntity> details = csdoMapper.findDetailsByHeaderId(id);

        // Reverse the posted reservations or inventory mutations
        if (header.isRequireGenerateCdo()) {
            // Reverse reservations by deleting them (since they were just allocated/forecasted)
            reservationMapper.deleteByDocNo(header.getDocNo());
        } else {
            // Reverse customer inventory mutations by posting negative quantities
            for (CsdoDetailEntity detail : details) {
                inventoryMutationMapper.upsertCustomerInventory(
                        header.getStore(),
                        header.getCustomerCode(),
                        header.getCustomerBranch(),
                        detail.getItemCode(),
                        detail.getQty().negate()  // Negative qty to reverse
                );
            }
        }

        csdoMapper.updateHeaderStatus(id, STATUS_REVERSED, Instant.now());
        return getById(id);
    }

    private String nextDocNo() {
        return "CSDO-" + String.format("%05d", sequence.getAndIncrement());
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private CsdoResponse toResponse(CsdoHeaderEntity header, List<CsdoDetailEntity> details) {
        List<CsdoResponseDetail> items = details.stream()
                .map(detail -> new CsdoResponseDetail(
                        detail.getId(),
                        detail.getItemCode(),
                        detail.getQty(),
                        detail.getUom()
                ))
                .toList();

        return new CsdoResponse(
                header.getId(),
                header.getDocNo(),
                header.getCsoId(),
                header.getCsoDocNo(),
                header.getCompany(),
                header.getStore(),
                header.getCustomerCode(),
                header.getCustomerBranch(),
                header.getCustomerEmail(),
                header.isRequireGenerateCdo(),
                header.getShippingMode(),
                header.getTransporter(),
                header.getStatus(),
                header.getCreatedBy(),
                header.getReleasedAt(),
                header.getCreatedAt(),
                header.getUpdatedAt(),
                items
        );
    }
}
