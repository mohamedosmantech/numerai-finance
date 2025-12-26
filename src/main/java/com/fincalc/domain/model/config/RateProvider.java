package com.fincalc.domain.model.config;

import java.util.Map;

/**
 * Configuration for external rate data providers.
 * Allows admin to configure multiple rate sources.
 */
public record RateProvider(
    String id,                // Unique identifier
    String name,              // Display name (e.g., "Federal Reserve", "European Central Bank")
    String type,              // Provider type: "FRED", "ECB", "BOE", "CUSTOM"
    String baseUrl,           // API base URL
    String apiKeyEnvVar,      // Environment variable name for API key
    Map<String, String> seriesMapping,  // Maps rate types to API series IDs
    int cacheDurationMinutes,
    boolean enabled
) {

    // Common rate types that providers can map to
    public static final String MORTGAGE_30_YEAR = "mortgage30Year";
    public static final String MORTGAGE_15_YEAR = "mortgage15Year";
    public static final String PRIME_RATE = "primeRate";
    public static final String CENTRAL_BANK_RATE = "centralBankRate";
    public static final String INFLATION_RATE = "inflationRate";
}
