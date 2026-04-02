package com.consignment.service.api;

import com.consignment.service.exception.BusinessRuleViolationException;
import com.consignment.service.model.settlement.SettlementBatchGenerateRequest;
import com.consignment.service.model.settlement.SettlementResponse;
import com.consignment.service.service.SettlementService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SettlementControllerTest {

    private final SettlementService settlementService = mock(SettlementService.class);
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new SettlementController(settlementService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void shouldGenerateSettlementBatch() throws Exception {
        when(settlementService.generateBatch(any(SettlementBatchGenerateRequest.class)))
                .thenReturn(new SettlementResponse(
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
                        "tester",
                        "WEEKLY-2026-03-01-2026-03-31-store01",
                        "Batch generated",
                        null,
                        null,
                        null,
                        Instant.parse("2026-03-27T00:00:00Z"),
                        Instant.parse("2026-03-27T00:00:00Z"),
                        List.of()
                ));

        String payload = """
                {
                  \"company\": \"COMP01\",
                  \"store\": \"STORE01\",
                  \"settlementType\": \"SUPPLIER\",
                  \"supplierCode\": \"SUPP01\",
                  \"supplierContract\": \"CONTRACT01\",
                  \"currency\": \"IDR\",
                  \"createdBy\": \"tester\",
                  \"fromDate\": \"2026-03-01\",
                  \"toDate\": \"2026-03-31\",
                  \"defaultUnitPrice\": 0,
                  \"referenceNo\": \"WEEKLY-2026-03-01-2026-03-31-store01\"
                }
                """;

        mockMvc.perform(post("/api/settlement/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("SET-1"))
                .andExpect(jsonPath("$.docNo").value("SETTL-00001"))
                .andExpect(jsonPath("$.referenceNo").value("WEEKLY-2026-03-01-2026-03-31-store01"));

        verify(settlementService).generateBatch(any(SettlementBatchGenerateRequest.class));
    }

    @Test
    void shouldReturnBusinessRuleViolationForInvalidBatchRequest() throws Exception {
        when(settlementService.generateBatch(any(SettlementBatchGenerateRequest.class)))
                .thenThrow(new BusinessRuleViolationException("No eligible source documents found for the requested period"));

        String payload = """
                {
                  \"company\": \"COMP01\",
                  \"store\": \"STORE01\",
                  \"settlementType\": \"SUPPLIER\",
                  \"supplierCode\": \"SUPP01\",
                  \"supplierContract\": \"CONTRACT01\",
                  \"currency\": \"IDR\",
                  \"createdBy\": \"tester\",
                  \"fromDate\": \"2026-03-01\",
                  \"toDate\": \"2026-03-31\",
                  \"defaultUnitPrice\": 0,
                  \"referenceNo\": \"WEEKLY-2026-03-01-2026-03-31-store01\"
                }
                """;

        mockMvc.perform(post("/api/settlement/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BUSINESS_RULE_VIOLATION"))
                .andExpect(jsonPath("$.message").value("No eligible source documents found for the requested period"));
    }
}