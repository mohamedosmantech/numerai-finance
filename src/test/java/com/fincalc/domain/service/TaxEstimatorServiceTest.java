package com.fincalc.domain.service;

import com.fincalc.domain.model.TaxEstimation;
import com.fincalc.domain.port.in.EstimateTaxesUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TaxEstimatorService")
class TaxEstimatorServiceTest {

    private TaxEstimatorService service;

    @BeforeEach
    void setUp() {
        service = new TaxEstimatorService();
    }

    @Test
    @DisplayName("should implement EstimateTaxesUseCase")
    void shouldImplementUseCase() {
        assertTrue(service instanceof EstimateTaxesUseCase);
    }

    @Test
    @DisplayName("should execute tax estimation command")
    void shouldExecuteCommand() {
        var command = new EstimateTaxesUseCase.Command(
                new BigDecimal("100000"),
                "single",
                null,
                "CA"
        );

        TaxEstimation result = service.execute(command);

        assertNotNull(result);
        assertEquals(new BigDecimal("100000"), result.grossIncome());
        assertEquals(TaxEstimation.FilingStatus.SINGLE, result.filingStatus());
        assertEquals("CA", result.state());
        assertTrue(result.federalTax().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(result.stateTax().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("should parse various filing status formats")
    void shouldParseFilingStatusFormats() {
        var marriedJoint = service.execute(new EstimateTaxesUseCase.Command(
                new BigDecimal("100000"), "married_joint", null, null
        ));
        assertEquals(TaxEstimation.FilingStatus.MARRIED_JOINT, marriedJoint.filingStatus());

        var headOfHousehold = service.execute(new EstimateTaxesUseCase.Command(
                new BigDecimal("100000"), "head_of_household", null, null
        ));
        assertEquals(TaxEstimation.FilingStatus.HEAD_OF_HOUSEHOLD, headOfHousehold.filingStatus());
    }

    @Test
    @DisplayName("should propagate invalid filing status error")
    void shouldPropagateInvalidFilingStatusError() {
        var invalidCommand = new EstimateTaxesUseCase.Command(
                new BigDecimal("100000"),
                "invalid_status",
                null,
                null
        );

        assertThrows(IllegalArgumentException.class, () -> service.execute(invalidCommand));
    }

    @Test
    @DisplayName("should handle null state")
    void shouldHandleNullState() {
        var command = new EstimateTaxesUseCase.Command(
                new BigDecimal("100000"),
                "single",
                null,
                null
        );

        TaxEstimation result = service.execute(command);

        assertEquals(BigDecimal.ZERO, result.stateTax());
    }

    @Test
    @DisplayName("should use standard deduction when no deductions provided")
    void shouldUseStandardDeduction() {
        var command = new EstimateTaxesUseCase.Command(
                new BigDecimal("100000"),
                "single",
                null,
                null
        );

        TaxEstimation result = service.execute(command);

        // Single standard deduction is $14,600
        assertEquals(new BigDecimal("14600"), result.deductions());
    }

    @Test
    @DisplayName("should use itemized deductions when provided")
    void shouldUseItemizedDeductions() {
        var command = new EstimateTaxesUseCase.Command(
                new BigDecimal("100000"),
                "single",
                new BigDecimal("25000"),
                null
        );

        TaxEstimation result = service.execute(command);

        assertEquals(new BigDecimal("25000"), result.deductions());
    }
}
