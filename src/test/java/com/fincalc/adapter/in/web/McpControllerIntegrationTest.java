package com.fincalc.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("MCP Controller Integration Tests")
class McpControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("SSE Connection")
    class SseConnection {

        @Test
        @DisplayName("should establish SSE connection")
        void shouldEstablishSseConnection() throws Exception {
            mockMvc.perform(get("/mcp")
                            .accept(MediaType.TEXT_EVENT_STREAM_VALUE))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Type", containsString("text/event-stream")));
        }
    }

    @Nested
    @DisplayName("JSON-RPC Messages")
    class JsonRpcMessages {

        private String getSessionId() throws Exception {
            MvcResult result = mockMvc.perform(get("/mcp")
                            .accept(MediaType.TEXT_EVENT_STREAM_VALUE))
                    .andReturn();

            String content = result.getResponse().getContentAsString();
            int start = content.indexOf("sessionId=") + 10;
            int end = content.indexOf("\n", start);
            if (end == -1) end = content.length();
            return content.substring(start, end).trim();
        }

        @Test
        @DisplayName("should handle initialize request")
        void shouldHandleInitializeRequest() throws Exception {
            String sessionId = getSessionId();

            Map<String, Object> request = Map.of(
                    "jsonrpc", "2.0",
                    "id", 1,
                    "method", "initialize",
                    "params", Map.of(
                            "protocolVersion", "2024-11-05",
                            "clientInfo", Map.of("name", "test-client", "version", "1.0")
                    )
            );

            mockMvc.perform(post("/mcp/messages")
                            .param("sessionId", sessionId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isAccepted())
                    .andExpect(jsonPath("$.jsonrpc").value("2.0"))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.result.serverInfo.name").value("fincalc-pro"))
                    .andExpect(jsonPath("$.result.capabilities.tools").exists());
        }

        @Test
        @DisplayName("should list available tools")
        void shouldListAvailableTools() throws Exception {
            String sessionId = getSessionId();

            Map<String, Object> request = Map.of(
                    "jsonrpc", "2.0",
                    "id", 2,
                    "method", "tools/list"
            );

            mockMvc.perform(post("/mcp/messages")
                            .param("sessionId", sessionId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isAccepted())
                    .andExpect(jsonPath("$.result.tools").isArray())
                    .andExpect(jsonPath("$.result.tools", hasSize(4)))
                    .andExpect(jsonPath("$.result.tools[*].name", hasItems(
                            "calculate_loan_payment",
                            "calculate_compound_interest",
                            "estimate_taxes",
                            "get_current_rates"
                    )));
        }

        @Test
        @DisplayName("should execute loan payment tool")
        void shouldExecuteLoanPaymentTool() throws Exception {
            String sessionId = getSessionId();

            Map<String, Object> request = Map.of(
                    "jsonrpc", "2.0",
                    "id", 3,
                    "method", "tools/call",
                    "params", Map.of(
                            "name", "calculate_loan_payment",
                            "arguments", Map.of(
                                    "principal", 300000,
                                    "annualRate", 6.5,
                                    "years", 30
                            )
                    )
            );

            mockMvc.perform(post("/mcp/messages")
                            .param("sessionId", sessionId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isAccepted())
                    .andExpect(jsonPath("$.result.content").isArray())
                    .andExpect(jsonPath("$.result.structuredContent.result.monthlyPayment").isNumber())
                    .andExpect(jsonPath("$.result.structuredContent.result.totalPayment").isNumber())
                    .andExpect(jsonPath("$.result.structuredContent.result.totalInterest").isNumber());
        }

        @Test
        @DisplayName("should execute compound interest tool")
        void shouldExecuteCompoundInterestTool() throws Exception {
            String sessionId = getSessionId();

            Map<String, Object> request = Map.of(
                    "jsonrpc", "2.0",
                    "id", 4,
                    "method", "tools/call",
                    "params", Map.of(
                            "name", "calculate_compound_interest",
                            "arguments", Map.of(
                                    "principal", 10000,
                                    "annualRate", 7,
                                    "years", 20,
                                    "monthlyContribution", 500
                            )
                    )
            );

            mockMvc.perform(post("/mcp/messages")
                            .param("sessionId", sessionId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isAccepted())
                    .andExpect(jsonPath("$.result.structuredContent.result.futureValue").isNumber())
                    .andExpect(jsonPath("$.result.structuredContent.result.totalContributions").isNumber())
                    .andExpect(jsonPath("$.result.structuredContent.result.totalInterestEarned").isNumber());
        }

        @Test
        @DisplayName("should execute tax estimation tool")
        void shouldExecuteTaxEstimationTool() throws Exception {
            String sessionId = getSessionId();

            Map<String, Object> request = Map.of(
                    "jsonrpc", "2.0",
                    "id", 5,
                    "method", "tools/call",
                    "params", Map.of(
                            "name", "estimate_taxes",
                            "arguments", Map.of(
                                    "grossIncome", 100000,
                                    "filingStatus", "single",
                                    "state", "CA"
                            )
                    )
            );

            mockMvc.perform(post("/mcp/messages")
                            .param("sessionId", sessionId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isAccepted())
                    .andExpect(jsonPath("$.result.structuredContent.result.federalTax").isNumber())
                    .andExpect(jsonPath("$.result.structuredContent.result.stateTax").isNumber())
                    .andExpect(jsonPath("$.result.structuredContent.result.takeHomePay").isNumber())
                    .andExpect(jsonPath("$.result.structuredContent.result.effectiveRate").isNumber());
        }

        @Test
        @DisplayName("should return error for invalid session")
        void shouldReturnErrorForInvalidSession() throws Exception {
            Map<String, Object> request = Map.of(
                    "jsonrpc", "2.0",
                    "id", 6,
                    "method", "tools/list"
            );

            mockMvc.perform(post("/mcp/messages")
                            .param("sessionId", "invalid-session-id")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("should return error for unknown method")
        void shouldReturnErrorForUnknownMethod() throws Exception {
            String sessionId = getSessionId();

            Map<String, Object> request = Map.of(
                    "jsonrpc", "2.0",
                    "id", 7,
                    "method", "unknown/method"
            );

            mockMvc.perform(post("/mcp/messages")
                            .param("sessionId", sessionId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isAccepted())
                    .andExpect(jsonPath("$.error").exists())
                    .andExpect(jsonPath("$.error.code").value(-32601));
        }

        @Test
        @DisplayName("should return error for invalid tool arguments")
        void shouldReturnErrorForInvalidToolArguments() throws Exception {
            String sessionId = getSessionId();

            Map<String, Object> request = Map.of(
                    "jsonrpc", "2.0",
                    "id", 8,
                    "method", "tools/call",
                    "params", Map.of(
                            "name", "calculate_loan_payment",
                            "arguments", Map.of(
                                    "principal", -100000,
                                    "annualRate", 6.5,
                                    "years", 30
                            )
                    )
            );

            mockMvc.perform(post("/mcp/messages")
                            .param("sessionId", sessionId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isAccepted())
                    .andExpect(jsonPath("$.error").exists());
        }
    }

    @Nested
    @DisplayName("Health Check")
    class HealthCheck {

        @Test
        @DisplayName("should return healthy status")
        void shouldReturnHealthyStatus() throws Exception {
            mockMvc.perform(get("/actuator/health"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("UP"));
        }
    }
}
