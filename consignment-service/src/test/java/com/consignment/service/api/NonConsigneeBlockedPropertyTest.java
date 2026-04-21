package com.consignment.service.api;

// Feature: consignee-role, Property 8: Non-Consignee Blocked

import com.consignment.service.config.ConsigneeContext;
import com.consignment.service.config.JwtAuthFilter;
import com.consignment.service.config.JwtUtil;
import com.consignment.service.config.SecurityConfig;
import com.consignment.service.filter.ApiLoggingFilter;
import com.consignment.service.filter.CorrelationIdFilter;
import com.consignment.service.logging.ApiLogRepository;
import com.consignment.service.logging.ApiLoggingService;
import com.consignment.service.persistence.mapper.ConsigneePurchaseOrderMapper;
import com.consignment.service.persistence.mapper.ConsignmentSetupMapper;
import com.consignment.service.persistence.mapper.CsoMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import net.jqwik.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Property-based test for Non-Consignee Blocked from /api/consignee/**.
 *
 * Validates: Requirements 6.5
 */
class NonConsigneeBlockedPropertyTest {

    private static final String TEST_SECRET = "test-secret-key-must-be-at-least-32-chars-long";

    // Lazily initialized Spring context shared across all property tries
    private static MockMvc sharedMockMvc;
    private static final Object LOCK = new Object();

    private MockMvc getMockMvc() throws Exception {
        if (sharedMockMvc == null) {
            synchronized (LOCK) {
                if (sharedMockMvc == null) {
                    SpringContextHolder holder = new SpringContextHolder();
                    TestContextManager tcm = new TestContextManager(SpringContextHolder.class);
                    tcm.prepareTestInstance(holder);
                    // Apply the Spring Security FilterChainProxy so authorization rules are enforced
                    FilterChainProxy filterChainProxy = holder.webApplicationContext
                            .getBean(FilterChainProxy.class);
                    sharedMockMvc = MockMvcBuilders
                            .webAppContextSetup(holder.webApplicationContext)
                            .addFilter(filterChainProxy)
                            .build();
                }
            }
        }
        return sharedMockMvc;
    }

    /**
     * Property 8: Non-Consignee Blocked from /api/consignee/**
     *
     * For any authenticated user that does NOT have ROLE_CONSIGNEE, any request to
     * /api/consignee/** SHALL return HTTP 403.
     *
     * Validates: Requirements 6.5
     */
    @Property(tries = 100)
    void nonConsigneeIsBlockedFromConsigneeEndpoints(@ForAll("nonConsigneeRoles") String role) throws Exception {
        String token = buildJwtWithRole(role);

        getMockMvc().perform(get("/api/consignee/products")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // ── Arbitraries ──────────────────────────────────────────────────────────

    @Provide
    Arbitrary<String> nonConsigneeRoles() {
        return Arbitraries.of(
                "ROLE_ADMIN",
                "ROLE_USER",
                "ROLE_MANAGER",
                "ROLE_VIEWER",
                "ROLE_OPERATOR"
        );
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private String buildJwtWithRole(String role) {
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject("test-user")
                .claim("roles", List.of(role))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3_600_000L))
                .signWith(key)
                .compact();
    }

    // ── Spring Context Holder ─────────────────────────────────────────────────

    /**
     * A separate class annotated with @SpringBootTest so TestContextManager can
     * bootstrap the Spring context and inject the WebApplicationContext.
     */
    @SpringBootTest(
            classes = TestApplication.class,
            webEnvironment = SpringBootTest.WebEnvironment.MOCK,
            properties = {
                    "jwt.secret=test-secret-key-must-be-at-least-32-chars-long",
                    "app.security.enabled=true",
                    "app.api-log.enabled=false",
                    "spring.sql.init.mode=never",
                    "eureka.client.enabled=false",
                    "spring.cloud.discovery.enabled=false",
                    "spring.autoconfigure.exclude="
                            + "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                            + "org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration,"
                            + "org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration,"
                            + "org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration"
            }
    )
    static class SpringContextHolder {
        @org.springframework.beans.factory.annotation.Autowired
        WebApplicationContext webApplicationContext;

        @MockBean
        ApiLogRepository apiLogRepository;

        @MockBean
        ConsignmentSetupMapper consignmentSetupMapper;

        @MockBean
        ConsigneePurchaseOrderMapper consigneePurchaseOrderMapper;

        @MockBean
        CsoMapper csoMapper;
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import({
            ConsigneeController.class,
            GlobalExceptionHandler.class,
            SecurityConfig.class,
            JwtAuthFilter.class,
            JwtUtil.class,
            ConsigneeContext.class,
            CorrelationIdFilter.class,
            ApiLoggingFilter.class,
            ApiLoggingService.class
    })
    static class TestApplication {
    }
}
