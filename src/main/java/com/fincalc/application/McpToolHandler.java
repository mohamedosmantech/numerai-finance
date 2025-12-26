package com.fincalc.application;

import com.fincalc.adapter.config.ChatGptRequestContext;
import com.fincalc.domain.model.CompoundInterestCalculation;
import com.fincalc.domain.model.LoanCalculation;
import com.fincalc.domain.model.TaxEstimation;
import com.fincalc.domain.port.in.CalculateCompoundInterestUseCase;
import com.fincalc.domain.port.in.CalculateLoanPaymentUseCase;
import com.fincalc.domain.port.in.EstimateTaxesUseCase;
import com.fincalc.domain.port.out.MarketRatePort;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Application service that handles MCP tool invocations.
 * Bridges the MCP protocol with domain use cases.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class McpToolHandler {

    private final CalculateLoanPaymentUseCase loanPaymentUseCase;
    private final CalculateCompoundInterestUseCase compoundInterestUseCase;
    private final EstimateTaxesUseCase taxesUseCase;
    private final MarketRatePort marketRatePort;
    private final Validator validator;

    /**
     * Get currency formatter based on the request context.
     */
    private NumberFormat getCurrencyFormatter(ChatGptRequestContext context) {
        if (context == null) {
            return NumberFormat.getCurrencyInstance(Locale.US);
        }
        return NumberFormat.getCurrencyInstance(context.getLocale());
    }

    /**
     * Validates a command using Bean Validation annotations.
     * Throws IllegalArgumentException if validation fails.
     */
    private <T> void validateCommand(T command) {
        Set<ConstraintViolation<T>> violations = validator.validate(command);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining("; "));
            throw new IllegalArgumentException(message);
        }
    }

    public List<Map<String, Object>> getToolDefinitions() {
        return List.of(
                createLoanPaymentToolDef(),
                createCompoundInterestToolDef(),
                createTaxEstimatorToolDef(),
                createCurrentRatesToolDef()
        );
    }

    /**
     * Execute a tool without request context (for testing or non-HTTP calls).
     */
    public Map<String, Object> executeTool(String toolName, Map<String, Object> arguments) {
        return executeTool(toolName, arguments, null);
    }

    /**
     * Execute a tool with ChatGPT request context (for HTTP calls with headers).
     */
    public Map<String, Object> executeTool(String toolName, Map<String, Object> arguments, ChatGptRequestContext context) {
        log.info("Executing tool: {} with arguments: {}, country: {}, language: {}",
                toolName, arguments,
                context != null ? context.getCountryCode() : "unknown",
                context != null ? context.getLanguageCode() : "unknown");

        return switch (toolName) {
            case "calculate_loan_payment" -> executeLoanPayment(arguments, context);
            case "calculate_compound_interest" -> executeCompoundInterest(arguments, context);
            case "estimate_taxes" -> executeTaxEstimation(arguments, context);
            case "get_current_rates" -> executeGetCurrentRates(arguments, context);
            default -> throw new IllegalArgumentException("Unknown tool: " + toolName);
        };
    }

    private Map<String, Object> executeLoanPayment(Map<String, Object> args, ChatGptRequestContext context) {
        var command = new CalculateLoanPaymentUseCase.Command(
                toBigDecimal(args.get("principal")),
                toBigDecimal(args.get("annualRate")),
                toInt(args.get("years"))
        );
        validateCommand(command);

        LoanCalculation result = loanPaymentUseCase.execute(command);
        NumberFormat currencyFmt = getCurrencyFormatter(context);

        String text = String.format("""
                **Loan Payment Calculator**

                **Loan Details:**
                - Principal: %s
                - Interest Rate: %s%% APR
                - Term: %d years (%d payments)

                **Results:**
                - Monthly Payment: **%s**
                - Total Payment: %s
                - Total Interest: %s

                ---
                _Calculation: Standard amortization formula | Provider: Numerai Finance_
                """,
                currencyFmt.format(result.principal()),
                result.annualRate(),
                result.years(),
                result.totalPayments(),
                currencyFmt.format(result.monthlyPayment()),
                currencyFmt.format(result.totalPayment()),
                currencyFmt.format(result.totalInterest())
        );

        return buildToolResponse("calculate_loan_payment", text,
                Map.of(
                        "input", Map.of(
                                "principal", result.principal(),
                                "annualRate", result.annualRate(),
                                "years", result.years()
                        ),
                        "result", Map.of(
                                "monthlyPayment", result.monthlyPayment(),
                                "totalPayment", result.totalPayment(),
                                "totalInterest", result.totalInterest()
                        ),
                        "dataSource", Map.of(
                                "provider", "Numerai Finance",
                                "method", "Standard Amortization Formula"
                        )
                ),
                context
        );
    }

    private Map<String, Object> executeCompoundInterest(Map<String, Object> args, ChatGptRequestContext context) {
        var command = new CalculateCompoundInterestUseCase.Command(
                toBigDecimal(args.get("principal")),
                toBigDecimal(args.get("annualRate")),
                toInt(args.get("years")),
                args.containsKey("compoundingFrequency") ? toInt(args.get("compoundingFrequency")) : 12,
                args.containsKey("monthlyContribution") ? toBigDecimal(args.get("monthlyContribution")) : BigDecimal.ZERO
        );
        validateCommand(command);

        CompoundInterestCalculation result = compoundInterestUseCase.execute(command);
        NumberFormat currencyFmt = getCurrencyFormatter(context);

        StringBuilder text = new StringBuilder(String.format("""
                **Investment Growth Calculator**

                **Investment Details:**
                - Initial Investment: %s
                - Annual Return: %s%% (%s compounding)
                - Time Period: %d years
                """,
                currencyFmt.format(result.principal()),
                result.annualRate(),
                result.compoundingLabel(),
                result.years()
        ));

        if (result.monthlyContribution().compareTo(BigDecimal.ZERO) > 0) {
            text.append(String.format("- Monthly Contribution: %s%n", currencyFmt.format(result.monthlyContribution())));
        }

        text.append(String.format("""

                **Results:**
                - Future Value: **%s**
                - Total Contributions: %s
                - Total Interest Earned: %s
                - Effective Annual Rate: %s%%

                ---
                _Calculation: Compound interest formula | Provider: Numerai Finance_
                """,
                currencyFmt.format(result.futureValue()),
                currencyFmt.format(result.totalContributions()),
                currencyFmt.format(result.totalInterestEarned()),
                result.effectiveAnnualRate()
        ));

        return buildToolResponse("calculate_compound_interest", text.toString(),
                Map.of(
                        "input", Map.of(
                                "principal", result.principal(),
                                "annualRate", result.annualRate(),
                                "years", result.years(),
                                "compoundingFrequency", result.compoundingFrequency(),
                                "monthlyContribution", result.monthlyContribution()
                        ),
                        "result", Map.of(
                                "futureValue", result.futureValue(),
                                "totalContributions", result.totalContributions(),
                                "totalInterestEarned", result.totalInterestEarned(),
                                "effectiveAnnualRate", result.effectiveAnnualRate()
                        ),
                        "dataSource", Map.of(
                                "provider", "Numerai Finance",
                                "method", "Compound Interest Formula with Future Value of Annuity"
                        )
                ),
                context
        );
    }

    private Map<String, Object> executeTaxEstimation(Map<String, Object> args, ChatGptRequestContext context) {
        // Determine country with fallback strategy
        String countryCode = determineCountryWithFallback(context);
        boolean usedFallback = isUsingFallbackCountry(context);

        var command = new EstimateTaxesUseCase.Command(
                toBigDecimal(args.get("grossIncome")),
                (String) args.get("filingStatus"),
                args.containsKey("deductions") ? toBigDecimal(args.get("deductions")) : null,
                args.containsKey("state") ? (String) args.get("state") : null
        );
        validateCommand(command);

        TaxEstimation result = taxesUseCase.execute(command);
        NumberFormat currencyFmt = getCurrencyFormatter(context);

        StringBuilder text = new StringBuilder();

        // Add fallback notice if applicable
        if (usedFallback) {
            text.append("""
                > **Note:** Country not detected from your request. Using **United States (US)** tax rates as default.
                > For other countries, please specify your country in the request.

                """);
        }

        text.append(String.format("""
                **Tax Estimator (2025)**

                **Income Details:**
                - Gross Income: %s
                - Filing Status: %s
                - Deductions: %s
                - Taxable Income: %s
                """,
                currencyFmt.format(result.grossIncome()),
                result.filingStatus().getDisplayName(),
                currencyFmt.format(result.deductions()),
                currencyFmt.format(result.taxableIncome())
        ));

        text.append(String.format("""

                **Tax Breakdown:**
                - Federal Tax: %s
                """, currencyFmt.format(result.federalTax())));

        if (result.state() != null && !result.state().isBlank()) {
            text.append(String.format("- State Tax (%s): %s%n",
                    result.state().toUpperCase(),
                    result.hasStateTax() ? currencyFmt.format(result.stateTax()) : "$0 (no state income tax)"
            ));
        }

        text.append(String.format("""

                **Summary:**
                - Total Tax: **%s**
                - Effective Tax Rate: %s%%
                - Take-Home Pay: **%s**
                """,
                currencyFmt.format(result.totalTax()),
                result.effectiveRate(),
                currencyFmt.format(result.takeHomePay())
        ));

        // Add data source reference
        text.append("""

                ---
                _Data source: IRS 2025 Tax Brackets | Provider: Numerai Finance_
                """);

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("grossIncome", result.grossIncome());
        inputMap.put("filingStatus", result.filingStatus().name().toLowerCase());
        inputMap.put("deductions", result.deductions());
        inputMap.put("state", result.state() != null ? result.state() : "");
        inputMap.put("country", countryCode);
        inputMap.put("usedFallbackCountry", usedFallback);

        return buildToolResponse("estimate_taxes", text.toString(),
                Map.of(
                        "input", inputMap,
                        "result", Map.of(
                                "federalTax", result.federalTax(),
                                "stateTax", result.stateTax(),
                                "totalTax", result.totalTax(),
                                "effectiveRate", result.effectiveRate(),
                                "takeHomePay", result.takeHomePay(),
                                "taxableIncome", result.taxableIncome()
                        ),
                        "dataSource", Map.of(
                                "provider", "Numerai Finance",
                                "authority", "IRS",
                                "taxYear", "2025",
                                "lastUpdated", "2025-01-01"
                        )
                ),
                context, usedFallback
        );
    }

    /**
     * Determine country code with fallback to US if not provided.
     */
    private String determineCountryWithFallback(ChatGptRequestContext context) {
        if (context == null) {
            return "US";
        }
        String country = context.getCountryCodeUpperCase();
        if (country == null || country.isBlank() || country.equals("UNKNOWN")) {
            return "US";
        }
        return country;
    }

    /**
     * Check if we're using fallback country (no country detected).
     */
    private boolean isUsingFallbackCountry(ChatGptRequestContext context) {
        if (context == null) {
            return true;
        }
        String country = context.getCountryCode();
        return country == null || country.isBlank() || country.equalsIgnoreCase("unknown");
    }

    private Map<String, Object> executeGetCurrentRates(Map<String, Object> args, ChatGptRequestContext context) {
        Map<String, BigDecimal> rates = marketRatePort.getAllCurrentRates();
        String lastUpdate = marketRatePort.getLastUpdateDate();

        StringBuilder text = new StringBuilder("""
                **Current Market Rates**

                **Mortgage Rates (National Average):**
                """);

        rates.forEach((name, rate) -> {
            String displayName = formatRateName(name);
            text.append(String.format("- %s: **%.2f%%**%n", displayName, rate));
        });

        text.append(String.format("""

                _Last updated: %s_
                _Source: Federal Reserve Economic Data (FRED)_

                Note: Actual rates may vary by lender, credit score, and location.

                ---
                _Data source: FRED API | Provider: Numerai Finance_
                """, lastUpdate));

        return buildToolResponse("get_current_rates", text.toString(),
                Map.of(
                        "lastUpdated", lastUpdate,
                        "rates", rates,
                        "dataSource", Map.of(
                                "provider", "Numerai Finance",
                                "source", "Federal Reserve Economic Data (FRED)",
                                "sourceUrl", "https://fred.stlouisfed.org",
                                "updateFrequency", "Daily"
                        )
                ),
                context
        );
    }

    private String formatRateName(String name) {
        return switch (name) {
            case "mortgage30Year" -> "30-Year Fixed Mortgage";
            case "mortgage15Year" -> "15-Year Fixed Mortgage";
            case "federalFundsRate" -> "Federal Funds Rate";
            case "primeRate" -> "Prime Rate";
            case "averageAutoLoan" -> "Average Auto Loan";
            case "averagePersonalLoan" -> "Average Personal Loan";
            case "averageCreditCard" -> "Average Credit Card APR";
            case "highYieldSavings" -> "High-Yield Savings";
            default -> name;
        };
    }

    /**
     * Builds a standardized tool response with OpenAI-specific metadata.
     */
    private Map<String, Object> buildToolResponse(String toolName, String textContent, Map<String, Object> structuredContent, ChatGptRequestContext context) {
        return buildToolResponse(toolName, textContent, structuredContent, context, false);
    }

    /**
     * Builds a standardized tool response with OpenAI-specific metadata and fallback indicator.
     */
    private Map<String, Object> buildToolResponse(String toolName, String textContent, Map<String, Object> structuredContent, ChatGptRequestContext context, boolean usedFallback) {
        var response = new LinkedHashMap<String, Object>();
        response.put("content", List.of(Map.of("type", "text", "text", textContent)));
        response.put("structuredContent", structuredContent);

        // Build metadata with context info
        var metaMap = new LinkedHashMap<String, Object>();
        metaMap.put("openai/visibility", "public");
        metaMap.put("openai/widgetAccessible", false);
        metaMap.put("tool", toolName);
        metaMap.put("provider", "Numerai Finance");
        metaMap.put("providerUrl", "https://numerai-finance-production.up.railway.app");

        // Include context info from ChatGPT headers
        if (context != null) {
            metaMap.put("country", context.getCountryCode());
            metaMap.put("language", context.getLanguageCode());
            metaMap.put("currency", context.getCurrency());
            if (context.getRequestId() != null) {
                metaMap.put("requestId", context.getRequestId());
            }
        }

        // Add fallback indicator if used
        if (usedFallback) {
            metaMap.put("usedFallbackCountry", true);
            metaMap.put("fallbackCountry", "US");
        }

        response.put("_meta", metaMap);
        return response;
    }

    private Map<String, Object> createLoanPaymentToolDef() {
        var def = new LinkedHashMap<String, Object>();
        def.put("name", "calculate_loan_payment");
        def.put("description", "Calculate monthly payment, total payment, and total interest for a loan or mortgage. Supports home loans, car loans, personal loans, and any amortized loan type.");
        def.put("inputSchema", Map.of(
                "type", "object",
                "properties", Map.of(
                        "principal", Map.of("type", "number", "description", "The loan amount in dollars (e.g., 300000 for $300,000)"),
                        "annualRate", Map.of("type", "number", "description", "Annual interest rate as a percentage (e.g., 6.5 for 6.5%)"),
                        "years", Map.of("type", "integer", "description", "Loan term in years (1-50)")
                ),
                "required", List.of("principal", "annualRate", "years"),
                "additionalProperties", false
        ));
        def.put("annotations", Map.of("destructiveHint", false, "readOnlyHint", true));
        def.put("securitySchemes", Map.of("type", "noauth"));
        return def;
    }

    private Map<String, Object> createCompoundInterestToolDef() {
        var def = new LinkedHashMap<String, Object>();
        def.put("name", "calculate_compound_interest");
        def.put("description", "Calculate future value of an investment with compound interest. Supports different compounding frequencies and optional recurring monthly contributions for retirement planning, savings goals, etc.");
        def.put("inputSchema", Map.of(
                "type", "object",
                "properties", Map.of(
                        "principal", Map.of("type", "number", "description", "Initial investment amount in dollars"),
                        "annualRate", Map.of("type", "number", "description", "Expected annual return rate as percentage (e.g., 7 for 7%)"),
                        "years", Map.of("type", "integer", "description", "Investment time horizon in years"),
                        "compoundingFrequency", Map.of("type", "integer", "description", "Times per year interest compounds (1=annually, 4=quarterly, 12=monthly, 365=daily). Default: 12"),
                        "monthlyContribution", Map.of("type", "number", "description", "Optional recurring monthly investment amount. Default: 0")
                ),
                "required", List.of("principal", "annualRate", "years"),
                "additionalProperties", false
        ));
        def.put("annotations", Map.of("destructiveHint", false, "readOnlyHint", true));
        def.put("securitySchemes", Map.of("type", "noauth"));
        return def;
    }

    private Map<String, Object> createTaxEstimatorToolDef() {
        var def = new LinkedHashMap<String, Object>();
        def.put("name", "estimate_taxes");
        def.put("description", "Estimate US federal and state income taxes for 2024. Calculates tax liability, effective tax rate, and take-home pay based on income and filing status.");
        def.put("inputSchema", Map.of(
                "type", "object",
                "properties", Map.of(
                        "grossIncome", Map.of("type", "number", "description", "Annual gross income in dollars"),
                        "filingStatus", Map.of("type", "string", "description", "Tax filing status", "enum", List.of("single", "married_joint", "married_separate", "head_of_household")),
                        "deductions", Map.of("type", "number", "description", "Total itemized deductions. If 0 or omitted, standard deduction is used"),
                        "state", Map.of("type", "string", "description", "Two-letter state code for state tax calculation (e.g., CA, NY, TX)")
                ),
                "required", List.of("grossIncome", "filingStatus"),
                "additionalProperties", false
        ));
        def.put("annotations", Map.of("destructiveHint", false, "readOnlyHint", true));
        def.put("securitySchemes", Map.of("type", "noauth"));
        return def;
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) return null;
        if (value instanceof BigDecimal bd) return bd;
        if (value instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        return new BigDecimal(value.toString());
    }

    private int toInt(Object value) {
        if (value == null) return 0;
        if (value instanceof Number n) return n.intValue();
        return Integer.parseInt(value.toString());
    }

    private Map<String, Object> createCurrentRatesToolDef() {
        var def = new LinkedHashMap<String, Object>();
        def.put("name", "get_current_rates");
        def.put("description", "Get current market interest rates including mortgage rates, Federal Reserve rates, and prime rate. Data sourced from Federal Reserve Economic Data (FRED) and updated regularly.");
        def.put("inputSchema", Map.of(
                "type", "object",
                "properties", Map.of(),
                "required", List.of(),
                "additionalProperties", false
        ));
        def.put("annotations", Map.of("destructiveHint", false, "readOnlyHint", true));
        def.put("securitySchemes", Map.of("type", "noauth"));
        return def;
    }
}
