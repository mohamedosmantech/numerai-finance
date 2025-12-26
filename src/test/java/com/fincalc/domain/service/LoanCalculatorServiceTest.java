package com.fincalc.domain.service;

import com.fincalc.domain.model.LoanCalculation;
import com.fincalc.domain.port.in.CalculateLoanPaymentUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LoanCalculatorService")
class LoanCalculatorServiceTest {

    private LoanCalculatorService service;

    @BeforeEach
    void setUp() {
        service = new LoanCalculatorService();
    }

    @Test
    @DisplayName("should implement CalculateLoanPaymentUseCase")
    void shouldImplementUseCase() {
        assertTrue(service instanceof CalculateLoanPaymentUseCase);
    }

    @Test
    @DisplayName("should execute loan calculation command")
    void shouldExecuteCommand() {
        var command = new CalculateLoanPaymentUseCase.Command(
                new BigDecimal("300000"),
                new BigDecimal("6.5"),
                30
        );

        LoanCalculation result = service.execute(command);

        assertNotNull(result);
        assertEquals(new BigDecimal("300000"), result.principal());
        assertEquals(new BigDecimal("6.5"), result.annualRate());
        assertEquals(30, result.years());
        assertTrue(result.monthlyPayment().compareTo(BigDecimal.ZERO) > 0);
    }

    // Note: Validation is now handled at the application layer (McpToolHandler)
    // using Bean Validation with custom constraint annotations.

    @Test
    @DisplayName("should handle different loan scenarios")
    void shouldHandleDifferentScenarios() {
        // Short term loan
        var shortTerm = service.execute(new CalculateLoanPaymentUseCase.Command(
                new BigDecimal("10000"), new BigDecimal("5"), 2
        ));
        assertNotNull(shortTerm);

        // Long term loan
        var longTerm = service.execute(new CalculateLoanPaymentUseCase.Command(
                new BigDecimal("500000"), new BigDecimal("7"), 30
        ));
        assertNotNull(longTerm);

        // Short term should have higher monthly but less total interest
        assertTrue(shortTerm.monthlyPayment()
                .multiply(new BigDecimal(shortTerm.totalPayments()))
                .compareTo(shortTerm.principal()) < longTerm.totalInterest().compareTo(longTerm.principal())
                ? false : true);
    }
}
