package com.fincalc.domain.service;

import com.fincalc.domain.model.CompoundInterestCalculation;
import com.fincalc.domain.port.in.CalculateCompoundInterestUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CompoundInterestService")
class CompoundInterestServiceTest {

    private CompoundInterestService service;

    @BeforeEach
    void setUp() {
        service = new CompoundInterestService();
    }

    @Test
    @DisplayName("should implement CalculateCompoundInterestUseCase")
    void shouldImplementUseCase() {
        assertTrue(service instanceof CalculateCompoundInterestUseCase);
    }

    @Test
    @DisplayName("should execute compound interest command")
    void shouldExecuteCommand() {
        var command = new CalculateCompoundInterestUseCase.Command(
                new BigDecimal("10000"),
                new BigDecimal("7"),
                20,
                12,
                new BigDecimal("500")
        );

        CompoundInterestCalculation result = service.execute(command);

        assertNotNull(result);
        assertEquals(new BigDecimal("10000"), result.principal());
        assertEquals(new BigDecimal("7"), result.annualRate());
        assertEquals(20, result.years());
        assertEquals(12, result.compoundingFrequency());
        assertEquals(new BigDecimal("500"), result.monthlyContribution());
        assertTrue(result.futureValue().compareTo(result.totalContributions()) > 0);
    }

    // Note: Validation is now handled at the application layer (McpToolHandler)
    // using Bean Validation with custom constraint annotations.

    @Test
    @DisplayName("should calculate without monthly contributions")
    void shouldCalculateWithoutContributions() {
        var command = new CalculateCompoundInterestUseCase.Command(
                new BigDecimal("10000"),
                new BigDecimal("10"),
                10,
                12,
                BigDecimal.ZERO
        );

        CompoundInterestCalculation result = service.execute(command);

        assertEquals(result.principal().setScale(2), result.totalContributions());
        assertTrue(result.totalInterestEarned().compareTo(BigDecimal.ZERO) > 0);
    }
}
