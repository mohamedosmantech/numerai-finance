package com.fincalc.domain.model.config;

import java.util.List;
import java.util.Map;

/**
 * Configurable response template for tool outputs.
 * Allows admin to customize response format, fields, and order.
 *
 * Example: Customize loan calculator response to include/exclude fields,
 * change labels, modify formatting per country/currency.
 */
public record ResponseTemplate(
    String toolName,              // e.g., "calculate_loan_payment"
    String countryCode,           // Optional: country-specific template
    String languageCode,          // Language for labels
    List<ResponseField> fields,   // Ordered list of fields to include
    String headerTemplate,        // Header markdown template
    String footerTemplate         // Footer markdown template
) {

    public record ResponseField(
        String fieldKey,          // e.g., "monthlyPayment"
        String label,             // Display label (localized)
        String format,            // Format: "currency", "percentage", "number", "text"
        boolean visible,          // Show/hide field
        int order                 // Display order
    ) {}

    /**
     * Create default template for a tool.
     */
    public static ResponseTemplate defaultForTool(String toolName) {
        return switch (toolName) {
            case "calculate_loan_payment" -> new ResponseTemplate(
                toolName, null, "en",
                List.of(
                    new ResponseField("principal", "Principal", "currency", true, 1),
                    new ResponseField("annualRate", "Interest Rate", "percentage", true, 2),
                    new ResponseField("years", "Term", "text", true, 3),
                    new ResponseField("monthlyPayment", "Monthly Payment", "currency", true, 4),
                    new ResponseField("totalPayment", "Total Payment", "currency", true, 5),
                    new ResponseField("totalInterest", "Total Interest", "currency", true, 6)
                ),
                "**Loan Payment Summary**",
                "_Powered by Numerai Finance_"
            );

            case "calculate_compound_interest" -> new ResponseTemplate(
                toolName, null, "en",
                List.of(
                    new ResponseField("principal", "Initial Investment", "currency", true, 1),
                    new ResponseField("annualRate", "Annual Return", "percentage", true, 2),
                    new ResponseField("years", "Time Period", "text", true, 3),
                    new ResponseField("monthlyContribution", "Monthly Contribution", "currency", true, 4),
                    new ResponseField("futureValue", "Future Value", "currency", true, 5),
                    new ResponseField("totalContributions", "Total Contributions", "currency", true, 6),
                    new ResponseField("totalInterestEarned", "Total Interest Earned", "currency", true, 7)
                ),
                "**Investment Growth Summary**",
                "_Powered by Numerai Finance_"
            );

            case "estimate_taxes" -> new ResponseTemplate(
                toolName, null, "en",
                List.of(
                    new ResponseField("grossIncome", "Gross Income", "currency", true, 1),
                    new ResponseField("filingStatus", "Filing Status", "text", true, 2),
                    new ResponseField("taxableIncome", "Taxable Income", "currency", true, 3),
                    new ResponseField("federalTax", "Federal Tax", "currency", true, 4),
                    new ResponseField("stateTax", "State Tax", "currency", true, 5),
                    new ResponseField("totalTax", "Total Tax", "currency", true, 6),
                    new ResponseField("effectiveRate", "Effective Rate", "percentage", true, 7),
                    new ResponseField("takeHomePay", "Take-Home Pay", "currency", true, 8)
                ),
                "**Tax Estimation**",
                "_Disclaimer: This is an estimate for educational purposes only._"
            );

            default -> new ResponseTemplate(
                toolName, null, "en",
                List.of(),
                "**Results**",
                ""
            );
        };
    }
}
