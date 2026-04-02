package com.consignment.service.api;

import com.consignment.service.config.SecurityConfig;
import com.consignment.service.exception.BusinessRuleViolationException;
import com.consignment.service.filter.ApiLoggingFilter;
import com.consignment.service.filter.CorrelationIdFilter;
import com.consignment.service.logging.ApiLogRepository;
import com.consignment.service.logging.ApiLoggingService;
import com.consignment.service.model.settlement.SettlementBatchGenerateRequest;
import com.consignment.service.model.settlement.SettlementResponse;
import com.consignment.service.service.SettlementService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        classes = SettlementControllerIntegrationTest.TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {
                "app.security.enabled=false",
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
@AutoConfigureMockMvc
class SettlementControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SettlementService settlementService;

    @MockBean
    private ApiLogRepository apiLogRepository;

    @Test
    void shouldGenerateSettlementBatchThroughHttpPipeline() throws Exception {
        when(settlementService.generateBatch(argThat(request ->
                "COMP01".equals(request.company())
                        && "STORE01".equals(request.store())
                        && "SUPPLIER".equals(request.settlementType())
                        && "SUPP01".equals(request.supplierCode())
                        && "CONTRACT01".equals(request.supplierContract())
                        && "WEEKLY-2026-03-01-2026-03-31-store01".equals(request.referenceNo())
        ))).thenReturn(new SettlementResponse(
                "SET-1",
                "SETTL-00001",
                "COMP01",
                "STORE01",
                "SUPPLIER",
                null,
                "SUPP01",
                "CONTRACT01",
                new BigDecimal("100.00"),
                "IDR",
                "HELD",
                "settlement-batch-job",
                "WEEKLY-2026-03-01-2026-03-31-store01",
                "Batch generated",
                null,
                null,
                null,
                Instant.parse("2026-03-27T00:00:00Z"),
                Instant.parse("2026-03-27T00:00:00Z"),
                List.of()
        ));

        mockMvc.perform(post("/api/settlement/generate")
                        .header(CorrelationIdFilter.CORRELATION_ID_HEADER, "corr-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "company": "COMP01",
                                  "store": "STORE01",
                                  "settlementType": "SUPPLIER",
                                  "supplierCode": "SUPP01",
                                  "supplierContract": "CONTRACT01",
                                  "currency": "IDR",
                                  "createdBy": "settlement-batch-job",
                                  "fromDate": "2026-03-01",
                                  "toDate": "2026-03-31",
                                  "defaultUnitPrice": 0,
                                  "referenceNo": "WEEKLY-2026-03-01-2026-03-31-store01"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(header().string(CorrelationIdFilter.CORRELATION_ID_HEADER, "corr-123"))
                .andExpect(jsonPath("$.id").value("SET-1"))
                .andExpect(jsonPath("$.docNo").value("SETTL-00001"))
                .andExpect(jsonPath("$.createdBy").value("settlement-batch-job"));

        verify(settlementService).generateBatch(argThat(request ->
                "COMP01".equals(request.company())
                        && "STORE01".equals(request.store())
                        && "SUPPLIER".equals(request.settlementType())
                        && "SUPP01".equals(request.supplierCode())
                        && "CONTRACT01".equals(request.supplierContract())
        ));
    }

    @Test
    void shouldReturnStructuredErrorThroughHttpPipeline() throws Exception {
        when(settlementService.generateBatch(argThat(SettlementBatchGenerateRequest.class::isInstance)))
                .thenThrow(new BusinessRuleViolationException("No eligible source documents found for the requested period"));

        mockMvc.perform(post("/api/settlement/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "company": "COMP01",
                                  "store": "STORE01",
                                  "settlementType": "SUPPLIER",
                                  "supplierCode": "SUPP01",
                                  "supplierContract": "CONTRACT01",
                                  "currency": "IDR",
                                  "createdBy": "settlement-batch-job",
                                  "fromDate": "2026-03-01",
                                  "toDate": "2026-03-31",
                                  "defaultUnitPrice": 0,
                                  "referenceNo": "WEEKLY-2026-03-01-2026-03-31-store01"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(header().exists(CorrelationIdFilter.CORRELATION_ID_HEADER))
                .andExpect(jsonPath("$.code").value("BUSINESS_RULE_VIOLATION"))
                .andExpect(jsonPath("$.message").value("No eligible source documents found for the requested period"));
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import({
            SettlementController.class,
            GlobalExceptionHandler.class,
            SecurityConfig.class,
            CorrelationIdFilter.class,
            ApiLoggingFilter.class,
            ApiLoggingService.class
    })
    static class TestApplication {
    }
}