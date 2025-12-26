package com.fincalc.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Domain model for loan payment calculations.
 * Immutable value object representing loan calculation results.
 */
public record LoanCalculation(
        BigDecimal principal,
        BigDecimal annualRate,
        int years,
        BigDecimal monthlyPayment,
        BigDecimal totalPayment,
        BigDecimal totalInterest
) {
    private static final int MONTHS_PER_YEAR = 12;
    private static final int SCALE = 2;

    public static LoanCalculation calculate(BigDecimal principal, BigDecimal annualRate, int years) {
        validateInputs(principal, annualRate, years);

        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(100 * MONTHS_PER_YEAR), 10, RoundingMode.HALF_UP);
        int numPayments = years * MONTHS_PER_YEAR;

        BigDecimal monthlyPayment;
        BigDecimal totalPayment;
        BigDecimal totalInterest;

        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            monthlyPayment = principal.divide(BigDecimal.valueOf(numPayments), SCALE, RoundingMode.HALF_UP);
            totalPayment = principal;
            totalInterest = BigDecimal.ZERO;
        } else {
            // M = P * [r(1+r)^n] / [(1+r)^n - 1]
            BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);
            BigDecimal onePlusRPowN = onePlusR.pow(numPayments);

            BigDecimal numerator = monthlyRate.multiply(onePlusRPowN);
            BigDecimal denominator = onePlusRPowN.subtract(BigDecimal.ONE);

            monthlyPayment = principal.multiply(numerator)
                    .divide(denominator, SCALE, RoundingMode.HALF_UP);
            totalPayment = monthlyPayment.multiply(BigDecimal.valueOf(numPayments))
                    .setScale(SCALE, RoundingMode.HALF_UP);
            totalInterest = totalPayment.subtract(principal).setScale(SCALE, RoundingMode.HALF_UP);
        }

        return new LoanCalculation(principal, annualRate, years, monthlyPayment, totalPayment, totalInterest);
    }

    private static void validateInputs(BigDecimal principal, BigDecimal annualRate, int years) {
        if (principal == null || principal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Principal must be positive");
        }
        if (annualRate == null || annualRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Annual rate must be non-negative");
        }
        if (years <= 0 || years > 50) {
            throw new IllegalArgumentException("Years must be between 1 and 50");
        }
    }

    public int totalPayments() {
        return years * MONTHS_PER_YEAR;
    }
}
