package com.fincalc.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TaxEstimation Domain Model")
class TaxEstimationTest {

    @Nested
    @DisplayName("Federal Tax Calculations")
    class FederalTaxCalculations {

        @Test
        @DisplayName("should calculate single filer tax correctly")
        void shouldCalculateSingleFilerTax() {
            var result = TaxEstimation.calculate(
                    new BigDecimal("100000"),
                    TaxEstimation.FilingStatus.SINGLE,
                    null,
                    null
            );

            assertEquals(new BigDecimal("100000"), result.grossIncome());
            assertEquals(TaxEstimation.FilingStatus.SINGLE, result.filingStatus());

            // Standard deduction for single is $14,600
            assertEquals(new BigDecimal("14600"), result.deductions());

            // Taxable income = 100000 - 14600 = 85400
            assertEquals(new BigDecimal("85400"), result.taxableIncome());

            // Federal tax should be calculated using 2024 brackets
            assertTrue(result.federalTax().compareTo(BigDecimal.ZERO) > 0);
            assertTrue(result.federalTax().compareTo(new BigDecimal("20000")) < 0);
        }

        @Test
        @DisplayName("should calculate married filing jointly tax correctly")
        void shouldCalculateMarriedJointTax() {
            var result = TaxEstimation.calculate(
                    new BigDecimal("200000"),
                    TaxEstimation.FilingStatus.MARRIED_JOINT,
                    null,
                    null
            );

            // Standard deduction for married joint is $29,200
            assertEquals(new BigDecimal("29200"), result.deductions());

            // Married filing jointly should have lower effective rate than single
            assertTrue(result.effectiveRate().compareTo(new BigDecimal("30")) < 0);
        }

        @Test
        @DisplayName("should use itemized deductions when higher")
        void shouldUseItemizedDeductions() {
            var result = TaxEstimation.calculate(
                    new BigDecimal("200000"),
                    TaxEstimation.FilingStatus.SINGLE,
                    new BigDecimal("25000"), // Higher than standard deduction
                    null
            );

            assertEquals(new BigDecimal("25000"), result.deductions());
            assertEquals(new BigDecimal("175000"), result.taxableIncome());
        }

        @ParameterizedTest
        @EnumSource(TaxEstimation.FilingStatus.class)
        @DisplayName("should handle all filing statuses")
        void shouldHandleAllFilingStatuses(TaxEstimation.FilingStatus status) {
            var result = TaxEstimation.calculate(
                    new BigDecimal("100000"),
                    status,
                    null,
                    null
            );

            assertNotNull(result);
            assertEquals(status, result.filingStatus());
            assertTrue(result.federalTax().compareTo(BigDecimal.ZERO) > 0);
        }
    }

    @Nested
    @DisplayName("State Tax Calculations")
    class StateTaxCalculations {

        @ParameterizedTest
        @CsvSource({
                "CA, 0.0930",
                "NY, 0.0685",
                "TX, 0.0",
                "FL, 0.0",
                "WA, 0.0",
                "IL, 0.0495"
        })
        @DisplayName("should apply correct state tax rates")
        void shouldApplyCorrectStateTaxRates(String state, double expectedRate) {
            var result = TaxEstimation.calculate(
                    new BigDecimal("100000"),
                    TaxEstimation.FilingStatus.SINGLE,
                    null,
                    state
            );

            BigDecimal expectedStateTax = result.taxableIncome()
                    .multiply(new BigDecimal(String.valueOf(expectedRate)))
                    .setScale(2, java.math.RoundingMode.HALF_UP);

            assertEquals(expectedStateTax, result.stateTax());
        }

        @ParameterizedTest
        @ValueSource(strings = {"TX", "FL", "WA", "NV", "WY", "SD", "AK", "TN", "NH"})
        @DisplayName("should have zero state tax for no-income-tax states")
        void shouldHaveZeroStateTaxForNoIncomeTaxStates(String state) {
            var result = TaxEstimation.calculate(
                    new BigDecimal("100000"),
                    TaxEstimation.FilingStatus.SINGLE,
                    null,
                    state
            );

            assertEquals(BigDecimal.ZERO.setScale(2), result.stateTax());
            assertFalse(result.hasStateTax());
        }

        @Test
        @DisplayName("should use default rate for unknown states")
        void shouldUseDefaultRateForUnknownStates() {
            var result = TaxEstimation.calculate(
                    new BigDecimal("100000"),
                    TaxEstimation.FilingStatus.SINGLE,
                    null,
                    "ZZ" // Unknown state code
            );

            // Default rate is 5%
            assertTrue(result.stateTax().compareTo(BigDecimal.ZERO) > 0);
        }

        @Test
        @DisplayName("should handle null state")
        void shouldHandleNullState() {
            var result = TaxEstimation.calculate(
                    new BigDecimal("100000"),
                    TaxEstimation.FilingStatus.SINGLE,
                    null,
                    null
            );

            assertEquals(BigDecimal.ZERO, result.stateTax());
        }
    }

    @Nested
    @DisplayName("Take-Home Pay Calculations")
    class TakeHomePayCalculations {

        @Test
        @DisplayName("should calculate correct take-home pay")
        void shouldCalculateCorrectTakeHomePay() {
            var result = TaxEstimation.calculate(
                    new BigDecimal("100000"),
                    TaxEstimation.FilingStatus.SINGLE,
                    null,
                    "CA"
            );

            assertEquals(result.grossIncome().subtract(result.totalTax()).setScale(2),
                    result.takeHomePay());
        }

        @Test
        @DisplayName("should calculate correct effective rate")
        void shouldCalculateCorrectEffectiveRate() {
            var result = TaxEstimation.calculate(
                    new BigDecimal("100000"),
                    TaxEstimation.FilingStatus.SINGLE,
                    null,
                    null
            );

            BigDecimal expectedRate = result.totalTax()
                    .divide(result.grossIncome(), 4, java.math.RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .setScale(2, java.math.RoundingMode.HALF_UP);

            assertEquals(expectedRate, result.effectiveRate());
        }
    }

    @Nested
    @DisplayName("Filing Status Parsing")
    class FilingStatusParsing {

        @ParameterizedTest
        @CsvSource({
                "single, SINGLE",
                "SINGLE, SINGLE",
                "married_joint, MARRIED_JOINT",
                "marriedjoint, MARRIED_JOINT",
                "married-joint, MARRIED_JOINT",
                "married_separate, MARRIED_SEPARATE",
                "head_of_household, HEAD_OF_HOUSEHOLD",
                "headofhousehold, HEAD_OF_HOUSEHOLD"
        })
        @DisplayName("should parse various filing status formats")
        void shouldParseVariousFormats(String input, TaxEstimation.FilingStatus expected) {
            assertEquals(expected, TaxEstimation.FilingStatus.fromString(input));
        }

        @Test
        @DisplayName("should throw for invalid filing status")
        void shouldThrowForInvalidFilingStatus() {
            assertThrows(IllegalArgumentException.class, () ->
                    TaxEstimation.FilingStatus.fromString("invalid_status")
            );
        }
    }

    @Nested
    @DisplayName("Input Validation")
    class InputValidation {

        @Test
        @DisplayName("should reject negative income")
        void shouldRejectNegativeIncome() {
            assertThrows(IllegalArgumentException.class, () ->
                    TaxEstimation.calculate(
                            new BigDecimal("-50000"),
                            TaxEstimation.FilingStatus.SINGLE,
                            null,
                            null
                    )
            );
        }

        @Test
        @DisplayName("should reject null filing status")
        void shouldRejectNullFilingStatus() {
            assertThrows(IllegalArgumentException.class, () ->
                    TaxEstimation.calculate(
                            new BigDecimal("50000"),
                            null,
                            null,
                            null
                    )
            );
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("should handle zero income")
        void shouldHandleZeroIncome() {
            var result = TaxEstimation.calculate(
                    BigDecimal.ZERO,
                    TaxEstimation.FilingStatus.SINGLE,
                    null,
                    null
            );

            assertEquals(BigDecimal.ZERO, result.taxableIncome());
            assertEquals(BigDecimal.ZERO.setScale(2), result.federalTax());
            assertEquals(BigDecimal.ZERO, result.effectiveRate());
        }

        @Test
        @DisplayName("should handle income below standard deduction")
        void shouldHandleIncomeBelowStandardDeduction() {
            var result = TaxEstimation.calculate(
                    new BigDecimal("10000"),
                    TaxEstimation.FilingStatus.SINGLE,
                    null,
                    null
            );

            // Standard deduction ($14,600) > income ($10,000)
            assertEquals(BigDecimal.ZERO, result.taxableIncome());
            assertEquals(BigDecimal.ZERO.setScale(2), result.federalTax());
        }

        @Test
        @DisplayName("should handle high income correctly")
        void shouldHandleHighIncomeCorrectly() {
            var result = TaxEstimation.calculate(
                    new BigDecimal("1000000"),
                    TaxEstimation.FilingStatus.SINGLE,
                    null,
                    "CA"
            );

            // High earners should hit top brackets
            assertTrue(result.federalTax().compareTo(new BigDecimal("250000")) > 0);
            assertTrue(result.effectiveRate().compareTo(new BigDecimal("35")) > 0);
        }
    }
}
