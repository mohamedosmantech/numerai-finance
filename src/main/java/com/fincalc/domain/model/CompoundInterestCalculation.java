package com.fincalc.domain.model;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Domain model for compound interest calculations.
 * Immutable value object representing investment growth results.
 */
public record CompoundInterestCalculation(
        BigDecimal principal,
        BigDecimal annualRate,
        int years,
        int compoundingFrequency,
        BigDecimal monthlyContribution,
        BigDecimal futureValue,
        BigDecimal totalContributions,
        BigDecimal totalInterestEarned,
        BigDecimal effectiveAnnualRate
) {
    private static final int SCALE = 2;
    private static final MathContext MC = new MathContext(15, RoundingMode.HALF_UP);

    public static CompoundInterestCalculation calculate(
            BigDecimal principal,
            BigDecimal annualRate,
            int years,
            int compoundingFrequency,
            BigDecimal monthlyContribution
    ) {
        validateInputs(principal, annualRate, years, compoundingFrequency, monthlyContribution);

        BigDecimal rate = annualRate.divide(BigDecimal.valueOf(100), MC);

        // Future value of principal: P * (1 + r/n)^(n*t)
        BigDecimal ratePerPeriod = rate.divide(BigDecimal.valueOf(compoundingFrequency), MC);
        BigDecimal onePlusRate = BigDecimal.ONE.add(ratePerPeriod);
        int totalPeriods = compoundingFrequency * years;
        BigDecimal principalFV = principal.multiply(pow(onePlusRate, totalPeriods));

        // Future value of monthly contributions
        BigDecimal contributionFV = BigDecimal.ZERO;
        if (monthlyContribution.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal monthlyRate = rate.divide(BigDecimal.valueOf(12), MC);
            int totalMonths = years * 12;
            BigDecimal onePlusMonthlyRate = BigDecimal.ONE.add(monthlyRate);
            BigDecimal fvFactor = pow(onePlusMonthlyRate, totalMonths).subtract(BigDecimal.ONE)
                    .divide(monthlyRate, MC);
            contributionFV = monthlyContribution.multiply(fvFactor);
        }

        BigDecimal futureValue = principalFV.add(contributionFV).setScale(SCALE, RoundingMode.HALF_UP);
        BigDecimal totalContributions = principal.add(monthlyContribution.multiply(BigDecimal.valueOf(years * 12L)))
                .setScale(SCALE, RoundingMode.HALF_UP);
        BigDecimal totalInterestEarned = futureValue.subtract(totalContributions).setScale(SCALE, RoundingMode.HALF_UP);

        // Effective Annual Rate: (1 + r/n)^n - 1
        BigDecimal effectiveRate = pow(onePlusRate, compoundingFrequency)
                .subtract(BigDecimal.ONE)
                .multiply(BigDecimal.valueOf(100))
                .setScale(SCALE, RoundingMode.HALF_UP);

        return new CompoundInterestCalculation(
                principal, annualRate, years, compoundingFrequency, monthlyContribution,
                futureValue, totalContributions, totalInterestEarned, effectiveRate
        );
    }

    private static BigDecimal pow(BigDecimal base, int exponent) {
        if (exponent == 0) return BigDecimal.ONE;
        BigDecimal result = BigDecimal.ONE;
        for (int i = 0; i < exponent; i++) {
            result = result.multiply(base, MC);
        }
        return result;
    }

    private static void validateInputs(BigDecimal principal, BigDecimal annualRate, int years,
                                       int compoundingFrequency, BigDecimal monthlyContribution) {
        if (principal == null || principal.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Principal must be non-negative");
        }
        if (annualRate == null || annualRate.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Annual rate must be non-negative");
        }
        if (years <= 0 || years > 100) {
            throw new IllegalArgumentException("Years must be between 1 and 100");
        }
        if (compoundingFrequency <= 0 || compoundingFrequency > 365) {
            throw new IllegalArgumentException("Compounding frequency must be between 1 and 365");
        }
        if (monthlyContribution == null || monthlyContribution.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Monthly contribution must be non-negative");
        }
    }

    public String compoundingLabel() {
        return switch (compoundingFrequency) {
            case 1 -> "annually";
            case 4 -> "quarterly";
            case 12 -> "monthly";
            case 365 -> "daily";
            default -> compoundingFrequency + "x/year";
        };
    }
}
