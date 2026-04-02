package com.consignment.service.service;

import com.consignment.service.client.InventoryClient;
import com.consignment.service.client.model.InventoryAvailability;
import com.consignment.service.client.model.InventoryReserveRequest;
import com.consignment.service.client.model.InventoryReserveResponse;
import com.consignment.service.domain.ConsignmentRecord;
import com.consignment.service.domain.ConsignmentStatus;
import com.consignment.service.exception.InvalidStateTransitionException;
import com.consignment.service.exception.ResourceNotFoundException;
import com.consignment.service.model.ConsignmentRequest;
import com.consignment.service.model.ConsignmentResponse;
import com.consignment.service.model.UpdateConsignmentStatusRequest;
import com.consignment.service.repository.ConsignmentRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ConsignmentDomainService {

    private final InventoryClient inventoryClient;
    private final ConsignmentRepository consignmentRepository;

    public ConsignmentDomainService(InventoryClient inventoryClient, ConsignmentRepository consignmentRepository) {
        this.inventoryClient = inventoryClient;
        this.consignmentRepository = consignmentRepository;
    }

    public ConsignmentResponse createRequest(ConsignmentRequest request) {
        InventoryAvailability availability = inventoryClient.getAvailability(request.sku());
        String requestId = UUID.randomUUID().toString();
        Instant now = Instant.now();

        if (availability.availableQty() < request.quantity()) {
            ConsignmentRecord rejected = new ConsignmentRecord(
                    requestId,
                    request.sku(),
                    request.quantity(),
                    request.requestStore(),
                    request.supplier(),
                    ConsignmentStatus.REJECTED,
                    "Insufficient available quantity",
                    now,
                    now
            );
            consignmentRepository.save(rejected);
            return toResponse(rejected);
        }

        InventoryReserveResponse reserveResponse = inventoryClient.reserve(
                new InventoryReserveRequest(request.sku(), request.quantity())
        );

        if (!reserveResponse.reserved()) {
            ConsignmentRecord rejected = new ConsignmentRecord(
                    requestId,
                    request.sku(),
                    request.quantity(),
                    request.requestStore(),
                    request.supplier(),
                    ConsignmentStatus.REJECTED,
                    "Reserve failed due to concurrent stock change",
                    now,
                    now
            );
            consignmentRepository.save(rejected);
            return toResponse(rejected);
        }

        ConsignmentRecord held = new ConsignmentRecord(
                requestId,
                request.sku(),
                request.quantity(),
                request.requestStore(),
                request.supplier(),
                ConsignmentStatus.HELD,
                "Consignment request created",
                now,
                now
        );

        consignmentRepository.save(held);
        return toResponse(held);
    }

    public ConsignmentResponse getById(String requestId) {
        ConsignmentRecord record = consignmentRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Consignment request not found: " + requestId));
        return toResponse(record);
    }

    public List<ConsignmentResponse> listAll() {
        return consignmentRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public ConsignmentResponse updateStatus(String requestId, UpdateConsignmentStatusRequest request) {
        ConsignmentRecord record = consignmentRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Consignment request not found: " + requestId));

        validateTransition(record.getStatus(), request.status());

        record.setStatus(request.status());
        if (request.message() != null && !request.message().isBlank()) {
            record.setMessage(request.message());
        }
        record.setUpdatedAt(Instant.now());

        consignmentRepository.save(record);
        return toResponse(record);
    }

    private void validateTransition(ConsignmentStatus current, ConsignmentStatus next) {
        if (current == ConsignmentStatus.REJECTED || current == ConsignmentStatus.CANCELLED) {
            throw new InvalidStateTransitionException("Terminal status cannot be changed: " + current);
        }
        if (current == ConsignmentStatus.UPDATED && next == ConsignmentStatus.HELD) {
            throw new InvalidStateTransitionException("Cannot move UPDATED back to HELD");
        }
    }

    private ConsignmentResponse toResponse(ConsignmentRecord record) {
        return new ConsignmentResponse(
                record.getRequestId(),
                record.getSku(),
                record.getQuantity(),
                record.getRequestStore(),
                record.getSupplier(),
                record.getStatus().name(),
                record.getMessage(),
                record.getCreatedAt(),
                record.getUpdatedAt()
        );
    }
}
