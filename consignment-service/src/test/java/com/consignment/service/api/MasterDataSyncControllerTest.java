package com.consignment.service.api;

import com.consignment.service.model.master.MasterSyncResponse;
import com.consignment.service.service.MasterDataSyncService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MasterDataSyncControllerTest {

    private final MasterDataSyncService masterDataSyncService = mock(MasterDataSyncService.class);
    private final MockMvc mockMvc = MockMvcBuilders
      .standaloneSetup(new MasterDataSyncController(masterDataSyncService))
      .setControllerAdvice(new GlobalExceptionHandler())
      .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldAcceptItemPriceSyncRequest() throws Exception {
  when(masterDataSyncService.sync(eq("item-prices"), anyList()))
                .thenReturn(new MasterSyncResponse("item-prices", 1, Instant.parse("2026-03-27T00:00:00Z")));

        String payload = """
                {
                  \"records\": [
                    {
                      \"code\": \"SKU-01\",
                      \"attributes\": {
                        \"company\": \"COMP01\",
                        \"store\": \"STORE01\",
                        \"supplierCode\": \"SUPP01\",
                        \"supplierContract\": \"CONTRACT01\",
                        \"unitPrice\": 12500.50,
                        \"currency\": \"IDR\",
                        \"effectiveFrom\": \"2026-03-01\"
                      }
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/acmm/master-sync/item-prices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entity").value("item-prices"))
                .andExpect(jsonPath("$.upserted").value(1));

              verify(masterDataSyncService).sync(eq("item-prices"), anyList());
    }

    @Test
    void shouldRejectInvalidItemPriceSyncRequest() throws Exception {
        String payload = objectMapper.writeValueAsString(java.util.Map.of("records", java.util.List.of(java.util.Map.of("attributes", java.util.Map.of("unitPrice", 1000)))));

        mockMvc.perform(post("/api/acmm/master-sync/item-prices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }
}