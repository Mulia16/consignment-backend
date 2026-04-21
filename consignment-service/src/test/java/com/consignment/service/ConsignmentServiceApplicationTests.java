package com.consignment.service;

import com.consignment.service.config.ConsigneeContext;
import com.consignment.service.config.JwtAuthFilter;
import com.consignment.service.config.JwtUtil;
import com.consignment.service.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that the security config beans (JwtAuthFilter, SecurityConfig, ConsigneeContext)
 * load correctly without requiring a database or external infrastructure.
 */
@SpringBootTest(
    classes = ConsignmentServiceApplicationTests.TestApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    properties = {
        "jwt.secret=test-secret-key-must-be-at-least-32-chars-long",
        "app.security.enabled=false",
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "spring.autoconfigure.exclude="
            + "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration,"
            + "org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration,"
            + "org.mybatis.spring.boot.autoconfigure.MybatisLanguageDriverAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration,"
            + "org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration,"
            + "io.awspring.cloud.autoconfigure.sqs.SqsAutoConfiguration,"
            + "org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration,"
            + "org.springframework.cloud.netflix.eureka.EurekaDiscoveryClientConfiguration,"
            + "org.springframework.cloud.openfeign.FeignAutoConfiguration"
    }
)
@AutoConfigureMockMvc
class ConsignmentServiceApplicationTests {

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import({
        SecurityConfig.class,
        JwtAuthFilter.class,
        JwtUtil.class,
        ConsigneeContext.class
    })
    static class TestApplication {
    }

    @Autowired
    private SecurityConfig securityConfig;

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ConsigneeContext consigneeContext;

    @Test
    void contextLoads() {
        assertThat(securityConfig).isNotNull();
        assertThat(jwtAuthFilter).isNotNull();
        assertThat(jwtUtil).isNotNull();
        assertThat(consigneeContext).isNotNull();
    }
}
