package com.fincalc.domain.model.config;

import java.util.List;

/**
 * Country configuration for financial calculations.
 * Extensible to support any country's financial regulations.
 */
public record Country(
    String code,              // ISO 3166-1 alpha-2 (e.g., "US", "GB", "DE")
    String name,              // Display name
    String currency,          // ISO 4217 currency code (e.g., "USD", "EUR")
    List<Region> regions,     // States/provinces/regions
    TaxSystem taxSystem,      // Federal/national tax configuration
    boolean hasRegionalTax,   // Whether regions have their own taxes
    String rateSource         // API source for rates (e.g., "FRED", "ECB", "BOE")
) {

    public record Region(
        String code,          // Region code (e.g., "CA", "NY")
        String name,          // Display name
        TaxBracket[] taxBrackets  // Regional tax brackets
    ) {}

    public record TaxSystem(
        String name,                  // e.g., "IRS", "HMRC", "BZSt"
        TaxBracket[] brackets,        // Federal tax brackets
        StandardDeduction[] deductions,
        String taxYear
    ) {}

    public record TaxBracket(
        double minIncome,
        double maxIncome,
        double rate,
        double baseTax
    ) {}

    public record StandardDeduction(
        String filingStatus,
        double amount
    ) {}
}
