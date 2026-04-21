package com.consignment.service.api;

// Feature: consignee-role, Property 9: JWT Without Store Claim Blocked

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Property-based test for JWT Without Store Claim Blocked on Consignee Endpoints.
 *
 * Validates: Requirements 12.3
 */
class JwtWithoutStoreClaimBlockedPropertyTest {

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
     * Property 9: JWT Without Store Claim Blocked on Consignee Endpoints
     *
     * For any JWT that does not contain a store claim (or contains a null/blank value),
     * any request to /api/consignee/** SHALL return HTTP 403 with message
     * "Store claim missing in token".
     *
     * Validates: Requirements 12.3
     */
    @Property(tries = 100)
    void jwtWithoutStoreClaimIsBlockedOnConsigneeEndpoints(
            @ForAll("storeClaimVariants") String storeClaim) throws Exception {

        String token = buildJwtWithConsigneeRoleAndStore(storeClaim);

        getMockMvc().perform(get("/api/consignee/products")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Store claim missing in token"));
    }

    // ── Arbitraries ──────────────────────────────────────────────────────────

    /**
     * Generates store claim values that are absent, null, or blank.
     * Uses a sentinel value "NO_STORE_CLAIM" to indicate the claim should be omitted entirely.
     */
    @Provide
    Arbitrary<String> storeClaimVariants() {
        // "NO_STORE_CLAIM" = omit the claim entirely; others are blank/whitespace values
        Arbitrary<String> noClaimSentinel = Arbitraries.just("NO_STORE_CLAIM");
        Arbitrary<String> blankStrings = Arbitraries.of("", " ", "   ", "\t", "\n");
        return Arbitraries.oneOf(noClaimSentinel, blankStrings);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private String buildJwtWithConsigneeRoleAndStore(String storeClaim) {
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        var builder = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject("consignee-user")
                .claim("roles", List.of("ROLE_CONSIGNEE"))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3_600_000L));

        // Only add store claim if it's not the sentinel (i.e., add blank/empty values)
        if (!"NO_STORE_CLAIM".equals(storeClaim)) {
            builder.claim("store", storeClaim);
        }

        return builder.signWith(key).compact();
    }

    // ── Spring Context Holder ─────────────────────────────────────────────────

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
        @Autowired
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
