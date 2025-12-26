package com.fincalc.domain.model.config;

/**
 * Currency configuration with formatting and conversion support.
 */
public record Currency(
    String code,              // ISO 4217 code (e.g., "USD", "EUR", "GBP")
    String symbol,            // Display symbol (e.g., "$", "€", "£")
    String name,              // Full name (e.g., "US Dollar")
    int decimalPlaces,        // Typically 2, but some currencies use 0 or 3
    boolean symbolPrefix,     // true for "$100", false for "100€"
    String thousandsSeparator,
    String decimalSeparator
) {

    /**
     * Format an amount in this currency.
     */
    public String format(double amount) {
        String formatted = String.format("%,." + decimalPlaces + "f", amount)
            .replace(",", "{{THOUSAND}}")
            .replace(".", "{{DECIMAL}}")
            .replace("{{THOUSAND}}", thousandsSeparator)
            .replace("{{DECIMAL}}", decimalSeparator);

        return symbolPrefix
            ? symbol + formatted
            : formatted + symbol;
    }
}
