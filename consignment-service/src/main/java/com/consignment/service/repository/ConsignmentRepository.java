package com.consignment.service.repository;

import com.consignment.service.domain.ConsignmentRecord;

import java.util.List;
import java.util.Optional;

public interface ConsignmentRepository {

    ConsignmentRecord save(ConsignmentRecord record);

    Optional<ConsignmentRecord> findById(String requestId);

    List<ConsignmentRecord> findAll();
}
