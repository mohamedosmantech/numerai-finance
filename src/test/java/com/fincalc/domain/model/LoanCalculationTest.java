package com.fincalc.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LoanCalculation Domain Model")
class LoanCalculationTest {

    @Nested
    @DisplayName("Valid Calculations")
    class ValidCalculations {

        @Test
        @DisplayName("should calculate 30-year mortgage correctly")
        void shouldCalculate30YearMortgage() {
            var result = LoanCalculation.calculate(
                    new BigDecimal("300000"),
                    new BigDecimal("6.5"),
                    30
            );

            assertEquals(new BigDecimal("300000"), result.principal());
            assertEquals(new BigDecimal("6.5"), result.annualRate());
            assertEquals(30, result.years());
            assertEquals(360, result.totalPayments());

            // Monthly payment should be approximately $1896.20
            assertTrue(result.monthlyPayment().compareTo(new BigDecimal("1890")) > 0);
            assertTrue(result.monthlyPayment().compareTo(new BigDecimal("1900")) < 0);

            // Total interest should be significant for a 30-year loan
            assertTrue(result.totalInterest().compareTo(result.principal()) > 0);

            // Total payment = monthly * 360
            assertEquals(0, result.monthlyPayment().multiply(new BigDecimal("360"))
                    .setScale(2, java.math.RoundingMode.HALF_UP)
                    .compareTo(result.totalPayment()));
        }

        @Test
        @DisplayName("should calculate 15-year mortgage correctly")
        void shouldCalculate15YearMortgage() {
            var result = LoanCalculation.calculate(
                    new BigDecimal("300000"),
                    new BigDecimal("6.0"),
                    15
            );

            assertEquals(180, result.totalPayments());
            // 15-year should have higher monthly but less total interest than 30-year
            assertTrue(result.monthlyPayment().compareTo(new BigDecimal("2500")) > 0);
        }

        @Test
        @DisplayName("should handle zero interest rate")
        void shouldHandleZeroInterestRate() {
            var result = LoanCalculation.calculate(
                    new BigDecimal("12000"),
                    BigDecimal.ZERO,
                    1
            );

            assertEquals(new BigDecimal("1000.00"), result.monthlyPayment());
            assertEquals(new BigDecimal("12000"), result.totalPayment());
            assertEquals(BigDecimal.ZERO, result.totalInterest());
        }

        @ParameterizedTest
        @DisplayName("should handle various loan amounts")
        @CsvSource({
                "10000, 5.0, 5",
                "500000, 7.25, 30",
                "1000000, 4.5, 20",
                "25000, 8.99, 3"
        })
        void shouldHandleVariousLoanAmounts(String principal, String rate, int years) {
            var result = LoanCalculation.calculate(
                    new BigDecimal(principal),
                    new BigDecimal(rate),
                    years
            );

            assertNotNull(result);
            assertTrue(result.monthlyPayment().compareTo(BigDecimal.ZERO) > 0);
            assertTrue(result.totalPayment().compareTo(result.principal()) >= 0);
        }
    }

    @Nested
    @DisplayName("Input Validation")
    class InputValidation {

        @Test
        @DisplayName("should reject null principal")
        void shouldRejectNullPrincipal() {
            assertThrows(IllegalArgumentException.class, () ->
                    LoanCalculation.calculate(null, new BigDecimal("6.0"), 30)
            );
        }

        @Test
        @DisplayName("should reject zero principal")
        void shouldRejectZeroPrincipal() {
            assertThrows(IllegalArgumentException.class, () ->
                    LoanCalculation.calculate(BigDecimal.ZERO, new BigDecimal("6.0"), 30)
            );
        }

        @Test
        @DisplayName("should reject negative principal")
        void shouldRejectNegativePrincipal() {
            assertThrows(IllegalArgumentException.class, () ->
                    LoanCalculation.calculate(new BigDecimal("-100000"), new BigDecimal("6.0"), 30)
            );
        }

        @Test
        @DisplayName("should reject negative interest rate")
        void shouldRejectNegativeRate() {
            assertThrows(IllegalArgumentException.class, () ->
                    LoanCalculation.calculate(new BigDecimal("100000"), new BigDecimal("-1.0"), 30)
            );
        }

        @Test
        @DisplayName("should reject zero years")
        void shouldRejectZeroYears() {
            assertThrows(IllegalArgumentException.class, () ->
                    LoanCalculation.calculate(new BigDecimal("100000"), new BigDecimal("6.0"), 0)
            );
        }

        @Test
        @DisplayName("should reject years over 50")
        void shouldRejectExcessiveYears() {
            assertThrows(IllegalArgumentException.class, () ->
                    LoanCalculation.calculate(new BigDecimal("100000"), new BigDecimal("6.0"), 51)
            );
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("should handle minimum valid inputs")
        void shouldHandleMinimumInputs() {
            var result = LoanCalculation.calculate(
                    new BigDecimal("1"),
                    new BigDecimal("0.01"),
                    1
            );

            assertNotNull(result);
            assertTrue(result.monthlyPayment().compareTo(BigDecimal.ZERO) > 0);
        }

        @Test
        @DisplayName("should handle maximum valid years")
        void shouldHandleMaximumYears() {
            var result = LoanCalculation.calculate(
                    new BigDecimal("100000"),
                    new BigDecimal("5.0"),
                    50
            );

            assertEquals(600, result.totalPayments());
            assertNotNull(result.monthlyPayment());
        }

        @Test
        @DisplayName("should handle high interest rate")
        void shouldHandleHighInterestRate() {
            var result = LoanCalculation.calculate(
                    new BigDecimal("10000"),
                    new BigDecimal("25.0"),
                    5
            );

            assertNotNull(result);
            // High rate should result in significant interest (at least 50% of principal)
            assertTrue(result.totalInterest().compareTo(result.principal().multiply(new BigDecimal("0.5"))) > 0);
        }
    }
}
