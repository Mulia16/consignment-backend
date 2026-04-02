package com.consignment.service.repository;

import com.consignment.service.domain.ConsignmentRecord;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryConsignmentRepository implements ConsignmentRepository {

    private final Map<String, ConsignmentRecord> storage = new ConcurrentHashMap<>();

    @Override
    public ConsignmentRecord save(ConsignmentRecord record) {
        storage.put(record.getRequestId(), record);
        return record;
    }

    @Override
    public Optional<ConsignmentRecord> findById(String requestId) {
        return Optional.ofNullable(storage.get(requestId));
    }

    @Override
    public List<ConsignmentRecord> findAll() {
        List<ConsignmentRecord> records = new ArrayList<>(storage.values());
        records.sort(Comparator.comparing(ConsignmentRecord::getCreatedAt).reversed());
        return records;
    }
}
