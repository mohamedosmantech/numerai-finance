package com.fincalc.adapter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for FinCalc validation rules.
 * Externalized configuration for business rules.
 */
@Component
@ConfigurationProperties(prefix = "fincalc.validation")
public class FinCalcProperties {

    private LoanProperties loan = new LoanProperties();
    private InvestmentProperties investment = new InvestmentProperties();
    private TaxProperties tax = new TaxProperties();

    public static class LoanProperties {
        private int minYears = 1;
        private int maxYears = 50;
        private double minRate = 0.01;
        private double maxRate = 50.0;

        public int getMinYears() { return minYears; }
        public void setMinYears(int minYears) { this.minYears = minYears; }
        public int getMaxYears() { return maxYears; }
        public void setMaxYears(int maxYears) { this.maxYears = maxYears; }
        public double getMinRate() { return minRate; }
        public void setMinRate(double minRate) { this.minRate = minRate; }
        public double getMaxRate() { return maxRate; }
        public void setMaxRate(double maxRate) { this.maxRate = maxRate; }
    }

    public static class InvestmentProperties {
        private int minYears = 1;
        private int maxYears = 100;
        private double minRate = 0.01;
        private double maxRate = 100.0;
        private int maxCompoundingFrequency = 365;

        public int getMinYears() { return minYears; }
        public void setMinYears(int minYears) { this.minYears = minYears; }
        public int getMaxYears() { return maxYears; }
        public void setMaxYears(int maxYears) { this.maxYears = maxYears; }
        public double getMinRate() { return minRate; }
        public void setMinRate(double minRate) { this.minRate = minRate; }
        public double getMaxRate() { return maxRate; }
        public void setMaxRate(double maxRate) { this.maxRate = maxRate; }
        public int getMaxCompoundingFrequency() { return maxCompoundingFrequency; }
        public void setMaxCompoundingFrequency(int maxCompoundingFrequency) { this.maxCompoundingFrequency = maxCompoundingFrequency; }
    }

    public static class TaxProperties {
        private double minIncome = 0;
        private double maxDeductions = 10_000_000;

        public double getMinIncome() { return minIncome; }
        public void setMinIncome(double minIncome) { this.minIncome = minIncome; }
        public double getMaxDeductions() { return maxDeductions; }
        public void setMaxDeductions(double maxDeductions) { this.maxDeductions = maxDeductions; }
    }

    public LoanProperties getLoan() { return loan; }
    public void setLoan(LoanProperties loan) { this.loan = loan; }
    public InvestmentProperties getInvestment() { return investment; }
    public void setInvestment(InvestmentProperties investment) { this.investment = investment; }
    public TaxProperties getTax() { return tax; }
    public void setTax(TaxProperties tax) { this.tax = tax; }
}
