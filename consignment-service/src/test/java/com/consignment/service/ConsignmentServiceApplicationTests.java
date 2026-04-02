package com.consignment.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = {
        "spring.main.lazy-initialization=true",
        "spring.sql.init.mode=never",
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false"
    }
)
class ConsignmentServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
