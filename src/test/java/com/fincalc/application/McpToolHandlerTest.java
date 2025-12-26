package com.fincalc.application;

import com.fincalc.domain.model.CompoundInterestCalculation;
import com.fincalc.domain.model.LoanCalculation;
import com.fincalc.domain.model.TaxEstimation;
import com.fincalc.domain.port.in.CalculateCompoundInterestUseCase;
import com.fincalc.domain.port.in.CalculateLoanPaymentUseCase;
import com.fincalc.domain.port.in.EstimateTaxesUseCase;
import com.fincalc.domain.port.out.MarketRatePort;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("McpToolHandler")
class McpToolHandlerTest {

    @Mock
    private CalculateLoanPaymentUseCase loanPaymentUseCase;

    @Mock
    private CalculateCompoundInterestUseCase compoundInterestUseCase;

    @Mock
    private EstimateTaxesUseCase taxesUseCase;

    @Mock
    private MarketRatePort marketRatePort;

    @Mock
    private Validator validator;

    @Mock
    private AnalyticsService analyticsService;

    private McpToolHandler handler;

    @BeforeEach
    void setUp() {
        // Mock validator to return no violations by default (lenient for tests that don't call validate)
        lenient().when(validator.validate(any())).thenReturn(Collections.emptySet());
        handler = new McpToolHandler(loanPaymentUseCase, compoundInterestUseCase, taxesUseCase, marketRatePort, validator, analyticsService);
    }

    @Nested
    @DisplayName("Tool Definitions")
    class ToolDefinitions {

        @Test
        @DisplayName("should return four tool definitions")
        void shouldReturnFourToolDefinitions() {
            List<Map<String, Object>> tools = handler.getToolDefinitions();

            assertEquals(4, tools.size());
        }

        @Test
        @DisplayName("should include loan payment tool")
        void shouldIncludeLoanPaymentTool() {
            List<Map<String, Object>> tools = handler.getToolDefinitions();

            boolean hasLoanTool = tools.stream()
                    .anyMatch(t -> "calculate_loan_payment".equals(t.get("name")));
            assertTrue(hasLoanTool);
        }

        @Test
        @DisplayName("should include compound interest tool")
        void shouldIncludeCompoundInterestTool() {
            List<Map<String, Object>> tools = handler.getToolDefinitions();

            boolean hasCompoundTool = tools.stream()
                    .anyMatch(t -> "calculate_compound_interest".equals(t.get("name")));
            assertTrue(hasCompoundTool);
        }

        @Test
        @DisplayName("should include tax estimator tool")
        void shouldIncludeTaxEstimatorTool() {
            List<Map<String, Object>> tools = handler.getToolDefinitions();

            boolean hasTaxTool = tools.stream()
                    .anyMatch(t -> "estimate_taxes".equals(t.get("name")));
            assertTrue(hasTaxTool);
        }

        @Test
        @DisplayName("should have proper input schema for each tool")
        void shouldHaveProperInputSchema() {
            List<Map<String, Object>> tools = handler.getToolDefinitions();

            for (Map<String, Object> tool : tools) {
                assertTrue(tool.containsKey("name"));
                assertTrue(tool.containsKey("description"));
                assertTrue(tool.containsKey("inputSchema"));

                @SuppressWarnings("unchecked")
                Map<String, Object> schema = (Map<String, Object>) tool.get("inputSchema");
                assertEquals("object", schema.get("type"));
                assertTrue(schema.containsKey("properties"));
                assertTrue(schema.containsKey("required"));
            }
        }
    }

    @Nested
    @DisplayName("Execute Loan Payment Tool")
    class ExecuteLoanPaymentTool {

        @Test
        @DisplayName("should execute loan payment calculation")
        void shouldExecuteLoanPaymentCalculation() {
            var mockResult = LoanCalculation.calculate(
                    new BigDecimal("300000"),
                    new BigDecimal("6.5"),
                    30
            );
            when(loanPaymentUseCase.execute(any())).thenReturn(mockResult);

            Map<String, Object> args = new HashMap<>();
            args.put("principal", 300000);
            args.put("annualRate", 6.5);
            args.put("years", 30);

            Map<String, Object> result = handler.executeTool("calculate_loan_payment", args);

            assertNotNull(result);
            assertTrue(result.containsKey("content"));
            assertTrue(result.containsKey("structuredContent"));
            verify(loanPaymentUseCase).execute(any());
        }

        @Test
        @DisplayName("should return structured content with input and result")
        void shouldReturnStructuredContent() {
            var mockResult = LoanCalculation.calculate(
                    new BigDecimal("300000"),
                    new BigDecimal("6.5"),
                    30
            );
            when(loanPaymentUseCase.execute(any())).thenReturn(mockResult);

            Map<String, Object> args = new HashMap<>();
            args.put("principal", 300000);
            args.put("annualRate", 6.5);
            args.put("years", 30);

            Map<String, Object> result = handler.executeTool("calculate_loan_payment", args);

            @SuppressWarnings("unchecked")
            Map<String, Object> structured = (Map<String, Object>) result.get("structuredContent");
            assertTrue(structured.containsKey("input"));
            assertTrue(structured.containsKey("result"));
        }
    }

    @Nested
    @DisplayName("Execute Compound Interest Tool")
    class ExecuteCompoundInterestTool {

        @Test
        @DisplayName("should execute compound interest calculation")
        void shouldExecuteCompoundInterestCalculation() {
            var mockResult = CompoundInterestCalculation.calculate(
                    new BigDecimal("10000"),
                    new BigDecimal("7"),
                    20,
                    12,
                    new BigDecimal("500")
            );
            when(compoundInterestUseCase.execute(any())).thenReturn(mockResult);

            Map<String, Object> args = new HashMap<>();
            args.put("principal", 10000);
            args.put("annualRate", 7);
            args.put("years", 20);
            args.put("compoundingFrequency", 12);
            args.put("monthlyContribution", 500);

            Map<String, Object> result = handler.executeTool("calculate_compound_interest", args);

            assertNotNull(result);
            verify(compoundInterestUseCase).execute(any());
        }

        @Test
        @DisplayName("should use default values for optional parameters")
        void shouldUseDefaultValues() {
            var mockResult = CompoundInterestCalculation.calculate(
                    new BigDecimal("10000"),
                    new BigDecimal("7"),
                    20,
                    12,
                    BigDecimal.ZERO
            );
            when(compoundInterestUseCase.execute(any())).thenReturn(mockResult);

            Map<String, Object> args = new HashMap<>();
            args.put("principal", 10000);
            args.put("annualRate", 7);
            args.put("years", 20);
            // Not providing compoundingFrequency and monthlyContribution

            Map<String, Object> result = handler.executeTool("calculate_compound_interest", args);

            assertNotNull(result);
            verify(compoundInterestUseCase).execute(any());
        }
    }

    @Nested
    @DisplayName("Execute Tax Estimation Tool")
    class ExecuteTaxEstimationTool {

        @Test
        @DisplayName("should execute tax estimation")
        void shouldExecuteTaxEstimation() {
            var mockResult = TaxEstimation.calculate(
                    new BigDecimal("100000"),
                    TaxEstimation.FilingStatus.SINGLE,
                    null,
                    "CA"
            );
            when(taxesUseCase.execute(any())).thenReturn(mockResult);

            Map<String, Object> args = new HashMap<>();
            args.put("grossIncome", 100000);
            args.put("filingStatus", "single");
            args.put("state", "CA");

            Map<String, Object> result = handler.executeTool("estimate_taxes", args);

            assertNotNull(result);
            verify(taxesUseCase).execute(any());
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {

        @Test
        @DisplayName("should throw for unknown tool")
        void shouldThrowForUnknownTool() {
            assertThrows(IllegalArgumentException.class, () ->
                    handler.executeTool("unknown_tool", new HashMap<>())
            );
        }

        @Test
        @DisplayName("should propagate domain validation errors")
        void shouldPropagateDomainErrors() {
            when(loanPaymentUseCase.execute(any()))
                    .thenThrow(new IllegalArgumentException("Invalid input"));

            Map<String, Object> args = new HashMap<>();
            args.put("principal", -100000);
            args.put("annualRate", 6.5);
            args.put("years", 30);

            assertThrows(IllegalArgumentException.class, () ->
                    handler.executeTool("calculate_loan_payment", args)
            );
        }
    }
}
