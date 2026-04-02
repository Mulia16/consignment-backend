package com.consignment.service.logging;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ApiLogRepository extends MongoRepository<ApiLogDocument, String> {
}
