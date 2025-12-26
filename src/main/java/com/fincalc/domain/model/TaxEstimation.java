package com.fincalc.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * Domain model for tax estimation calculations.
 * Uses 2024 US federal tax brackets.
 */
public record TaxEstimation(
        BigDecimal grossIncome,
        FilingStatus filingStatus,
        BigDecimal deductions,
        String state,
        BigDecimal taxableIncome,
        BigDecimal federalTax,
        BigDecimal stateTax,
        BigDecimal totalTax,
        BigDecimal effectiveRate,
        BigDecimal takeHomePay
) {
    private static final int SCALE = 2;

    public enum FilingStatus {
        SINGLE("Single", 14600),
        MARRIED_JOINT("Married Filing Jointly", 29200),
        MARRIED_SEPARATE("Married Filing Separately", 14600),
        HEAD_OF_HOUSEHOLD("Head of Household", 21900);

        private final String displayName;
        private final int standardDeduction;

        FilingStatus(String displayName, int standardDeduction) {
            this.displayName = displayName;
            this.standardDeduction = standardDeduction;
        }

        public String getDisplayName() { return displayName; }
        public int getStandardDeduction() { return standardDeduction; }

        public static FilingStatus fromString(String value) {
            return switch (value.toLowerCase().replace("-", "_")) {
                case "single" -> SINGLE;
                case "married_joint", "marriedjoint" -> MARRIED_JOINT;
                case "married_separate", "marriedseparate" -> MARRIED_SEPARATE;
                case "head_of_household", "headofhousehold" -> HEAD_OF_HOUSEHOLD;
                default -> throw new IllegalArgumentException("Invalid filing status: " + value);
            };
        }
    }

    // 2024 Federal Tax Brackets
    private static final double[][] SINGLE_BRACKETS = {
            {11600, 0.10}, {47150, 0.12}, {100525, 0.22}, {191950, 0.24},
            {243725, 0.32}, {609350, 0.35}, {Double.MAX_VALUE, 0.37}
    };

    private static final double[][] MARRIED_JOINT_BRACKETS = {
            {23200, 0.10}, {94300, 0.12}, {201050, 0.22}, {383900, 0.24},
            {487450, 0.32}, {731200, 0.35}, {Double.MAX_VALUE, 0.37}
    };

    private static final double[][] HEAD_OF_HOUSEHOLD_BRACKETS = {
            {16550, 0.10}, {63100, 0.12}, {100500, 0.22}, {191950, 0.24},
            {243700, 0.32}, {609350, 0.35}, {Double.MAX_VALUE, 0.37}
    };

    // State tax rates (simplified flat rates)
    private static final Map<String, Double> STATE_RATES = Map.ofEntries(
            Map.entry("CA", 0.0930), Map.entry("NY", 0.0685), Map.entry("NJ", 0.0637),
            Map.entry("IL", 0.0495), Map.entry("MA", 0.0500), Map.entry("PA", 0.0307),
            Map.entry("OH", 0.0399), Map.entry("GA", 0.0549), Map.entry("NC", 0.0525),
            Map.entry("AZ", 0.0250), Map.entry("CO", 0.0440),
            // No state income tax
            Map.entry("TX", 0.0), Map.entry("FL", 0.0), Map.entry("WA", 0.0),
            Map.entry("NV", 0.0), Map.entry("WY", 0.0), Map.entry("SD", 0.0),
            Map.entry("AK", 0.0), Map.entry("TN", 0.0), Map.entry("NH", 0.0)
    );

    public static TaxEstimation calculate(BigDecimal grossIncome, FilingStatus filingStatus,
                                          BigDecimal deductions, String state) {
        validateInputs(grossIncome, filingStatus);

        BigDecimal actualDeductions = (deductions != null && deductions.compareTo(BigDecimal.ZERO) > 0)
                ? deductions
                : BigDecimal.valueOf(filingStatus.getStandardDeduction());

        BigDecimal taxableIncome = grossIncome.subtract(actualDeductions).max(BigDecimal.ZERO);

        BigDecimal federalTax = calculateFederalTax(taxableIncome, filingStatus);
        BigDecimal stateTax = calculateStateTax(taxableIncome, state);
        BigDecimal totalTax = federalTax.add(stateTax).setScale(SCALE, RoundingMode.HALF_UP);

        BigDecimal effectiveRate = grossIncome.compareTo(BigDecimal.ZERO) > 0
                ? totalTax.divide(grossIncome, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).setScale(SCALE, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal takeHomePay = grossIncome.subtract(totalTax).setScale(SCALE, RoundingMode.HALF_UP);

        return new TaxEstimation(grossIncome, filingStatus, actualDeductions, state,
                taxableIncome, federalTax, stateTax, totalTax, effectiveRate, takeHomePay);
    }

    private static BigDecimal calculateFederalTax(BigDecimal taxableIncome, FilingStatus status) {
        double[][] brackets = switch (status) {
            case SINGLE, MARRIED_SEPARATE -> SINGLE_BRACKETS;
            case MARRIED_JOINT -> MARRIED_JOINT_BRACKETS;
            case HEAD_OF_HOUSEHOLD -> HEAD_OF_HOUSEHOLD_BRACKETS;
        };

        double tax = 0;
        double remaining = taxableIncome.doubleValue();
        double previousLimit = 0;

        for (double[] bracket : brackets) {
            double limit = bracket[0];
            double rate = bracket[1];
            double taxableInBracket = Math.min(remaining, limit - previousLimit);

            if (taxableInBracket > 0) {
                tax += taxableInBracket * rate;
                remaining -= taxableInBracket;
            }

            if (remaining <= 0) break;
            previousLimit = limit;
        }

        return BigDecimal.valueOf(tax).setScale(SCALE, RoundingMode.HALF_UP);
    }

    private static BigDecimal calculateStateTax(BigDecimal taxableIncome, String state) {
        if (state == null || state.isBlank()) {
            return BigDecimal.ZERO;
        }

        double rate = STATE_RATES.getOrDefault(state.toUpperCase(), 0.05);
        return taxableIncome.multiply(BigDecimal.valueOf(rate)).setScale(SCALE, RoundingMode.HALF_UP);
    }

    private static void validateInputs(BigDecimal grossIncome, FilingStatus filingStatus) {
        if (grossIncome == null || grossIncome.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Gross income must be non-negative");
        }
        if (filingStatus == null) {
            throw new IllegalArgumentException("Filing status is required");
        }
    }

    public boolean hasStateTax() {
        return stateTax.compareTo(BigDecimal.ZERO) > 0;
    }
}
