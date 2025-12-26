package com.fincalc.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CompoundInterestCalculation Domain Model")
class CompoundInterestCalculationTest {

    @Nested
    @DisplayName("Valid Calculations")
    class ValidCalculations {

        @Test
        @DisplayName("should calculate simple compound interest correctly")
        void shouldCalculateSimpleCompoundInterest() {
            var result = CompoundInterestCalculation.calculate(
                    new BigDecimal("10000"),
                    new BigDecimal("7"),
                    10,
                    12,
                    BigDecimal.ZERO
            );

            assertEquals(new BigDecimal("10000"), result.principal());
            assertEquals(new BigDecimal("7"), result.annualRate());
            assertEquals(10, result.years());
            assertEquals(12, result.compoundingFrequency());

            // $10,000 at 7% for 10 years should be approximately $20,096
            assertTrue(result.futureValue().compareTo(new BigDecimal("19000")) > 0);
            assertTrue(result.futureValue().compareTo(new BigDecimal("21000")) < 0);

            // Interest earned should be positive
            assertTrue(result.totalInterestEarned().compareTo(BigDecimal.ZERO) > 0);

            // Total contributions = principal (no monthly contribution)
            assertEquals(result.principal().setScale(2), result.totalContributions());
        }

        @Test
        @DisplayName("should calculate with monthly contributions")
        void shouldCalculateWithMonthlyContributions() {
            var result = CompoundInterestCalculation.calculate(
                    new BigDecimal("10000"),
                    new BigDecimal("7"),
                    20,
                    12,
                    new BigDecimal("500")
            );

            // Total contributions = 10000 + (500 * 12 * 20) = 130,000
            assertEquals(new BigDecimal("130000.00"), result.totalContributions());

            // Future value should be significantly higher with contributions
            assertTrue(result.futureValue().compareTo(new BigDecimal("200000")) > 0);

            // Interest earned = future value - contributions
            assertEquals(result.futureValue().subtract(result.totalContributions())
                    .setScale(2, java.math.RoundingMode.HALF_UP), result.totalInterestEarned());
        }

        @Test
        @DisplayName("should calculate effective annual rate correctly")
        void shouldCalculateEffectiveAnnualRate() {
            var result = CompoundInterestCalculation.calculate(
                    new BigDecimal("10000"),
                    new BigDecimal("12"),
                    1,
                    12,
                    BigDecimal.ZERO
            );

            // EAR for 12% compounded monthly should be approximately 12.68%
            assertTrue(result.effectiveAnnualRate().compareTo(new BigDecimal("12.5")) > 0);
            assertTrue(result.effectiveAnnualRate().compareTo(new BigDecimal("13")) < 0);
        }

        @ParameterizedTest
        @DisplayName("should handle different compounding frequencies")
        @ValueSource(ints = {1, 4, 12, 365})
        void shouldHandleDifferentCompoundingFrequencies(int frequency) {
            var result = CompoundInterestCalculation.calculate(
                    new BigDecimal("10000"),
                    new BigDecimal("10"),
                    5,
                    frequency,
                    BigDecimal.ZERO
            );

            assertNotNull(result);
            assertEquals(frequency, result.compoundingFrequency());
            assertTrue(result.futureValue().compareTo(result.principal()) > 0);
        }
    }

    @Nested
    @DisplayName("Compounding Labels")
    class CompoundingLabels {

        @ParameterizedTest
        @CsvSource({
                "1, annually",
                "4, quarterly",
                "12, monthly",
                "365, daily",
                "2, 2x/year",
                "52, 52x/year"
        })
        @DisplayName("should return correct compounding label")
        void shouldReturnCorrectCompoundingLabel(int frequency, String expectedLabel) {
            var result = CompoundInterestCalculation.calculate(
                    new BigDecimal("1000"),
                    new BigDecimal("5"),
                    1,
                    frequency,
                    BigDecimal.ZERO
            );

            assertEquals(expectedLabel, result.compoundingLabel());
        }
    }

    @Nested
    @DisplayName("Input Validation")
    class InputValidation {

        @Test
        @DisplayName("should reject negative principal")
        void shouldRejectNegativePrincipal() {
            assertThrows(IllegalArgumentException.class, () ->
                    CompoundInterestCalculation.calculate(
                            new BigDecimal("-1000"),
                            new BigDecimal("5"),
                            10,
                            12,
                            BigDecimal.ZERO
                    )
            );
        }

        @Test
        @DisplayName("should reject negative rate")
        void shouldRejectNegativeRate() {
            assertThrows(IllegalArgumentException.class, () ->
                    CompoundInterestCalculation.calculate(
                            new BigDecimal("1000"),
                            new BigDecimal("-5"),
                            10,
                            12,
                            BigDecimal.ZERO
                    )
            );
        }

        @Test
        @DisplayName("should reject zero years")
        void shouldRejectZeroYears() {
            assertThrows(IllegalArgumentException.class, () ->
                    CompoundInterestCalculation.calculate(
                            new BigDecimal("1000"),
                            new BigDecimal("5"),
                            0,
                            12,
                            BigDecimal.ZERO
                    )
            );
        }

        @Test
        @DisplayName("should reject years over 100")
        void shouldRejectExcessiveYears() {
            assertThrows(IllegalArgumentException.class, () ->
                    CompoundInterestCalculation.calculate(
                            new BigDecimal("1000"),
                            new BigDecimal("5"),
                            101,
                            12,
                            BigDecimal.ZERO
                    )
            );
        }

        @Test
        @DisplayName("should reject zero compounding frequency")
        void shouldRejectZeroCompoundingFrequency() {
            assertThrows(IllegalArgumentException.class, () ->
                    CompoundInterestCalculation.calculate(
                            new BigDecimal("1000"),
                            new BigDecimal("5"),
                            10,
                            0,
                            BigDecimal.ZERO
                    )
            );
        }

        @Test
        @DisplayName("should reject negative monthly contribution")
        void shouldRejectNegativeMonthlyContribution() {
            assertThrows(IllegalArgumentException.class, () ->
                    CompoundInterestCalculation.calculate(
                            new BigDecimal("1000"),
                            new BigDecimal("5"),
                            10,
                            12,
                            new BigDecimal("-100")
                    )
            );
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("should handle zero principal with contributions")
        void shouldHandleZeroPrincipalWithContributions() {
            var result = CompoundInterestCalculation.calculate(
                    BigDecimal.ZERO,
                    new BigDecimal("7"),
                    10,
                    12,
                    new BigDecimal("500")
            );

            // Total contributions = 500 * 12 * 10 = 60,000
            assertEquals(new BigDecimal("60000.00"), result.totalContributions());
            assertTrue(result.futureValue().compareTo(result.totalContributions()) > 0);
        }

        @Test
        @DisplayName("should handle very low interest rate")
        void shouldHandleVeryLowInterestRate() {
            var result = CompoundInterestCalculation.calculate(
                    new BigDecimal("10000"),
                    new BigDecimal("0.01"), // Very low but not zero to avoid division by zero
                    10,
                    12,
                    new BigDecimal("100")
            );

            // Total contributions = 10000 + (100 * 12 * 10) = 22,000
            assertEquals(new BigDecimal("22000.00"), result.totalContributions());
            // Future value should be slightly higher due to tiny interest
            assertTrue(result.futureValue().compareTo(result.totalContributions()) >= 0);
        }

        @Test
        @DisplayName("should handle long investment horizon")
        void shouldHandleLongInvestmentHorizon() {
            var result = CompoundInterestCalculation.calculate(
                    new BigDecimal("10000"),
                    new BigDecimal("10"),
                    50,
                    12,
                    BigDecimal.ZERO
            );

            // $10,000 at 10% for 50 years should grow significantly
            assertTrue(result.futureValue().compareTo(new BigDecimal("1000000")) > 0);
        }
    }
}
