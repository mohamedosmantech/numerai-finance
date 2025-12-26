package com.fincalc.adapter.out.rates;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

/**
 * Client for Federal Reserve Economic Data (FRED) API.
 * Fetches real-time interest rates from the Federal Reserve.
 *
 * Free API: https://fred.stlouisfed.org/docs/api/fred/
 *
 * Series IDs:
 * - MORTGAGE30US: 30-Year Fixed Rate Mortgage Average
 * - MORTGAGE15US: 15-Year Fixed Rate Mortgage Average
 * - FEDFUNDS: Federal Funds Effective Rate
 * - DPRIME: Bank Prime Loan Rate
 */
@Slf4j
@Component
public class FredApiClient {

    private static final String FRED_BASE_URL = "https://api.stlouisfed.org/fred/series/observations";

    // Series IDs for different rates
    public static final String MORTGAGE_30_YEAR = "MORTGAGE30US";
    public static final String MORTGAGE_15_YEAR = "MORTGAGE15US";
    public static final String FEDERAL_FUNDS_RATE = "FEDFUNDS";
    public static final String PRIME_RATE = "DPRIME";

    private final RestTemplate restTemplate;

    @Value("${fincalc.rates.fred-api-key:}")
    private String apiKey;

    public FredApiClient() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Fetch the latest observation for a FRED series.
     * Results are cached for 1 hour to avoid rate limiting.
     */
    @Cacheable(value = "fredRates", key = "#seriesId")
    public Optional<BigDecimal> getLatestRate(String seriesId) {
        if (apiKey == null || apiKey.isBlank()) {
            log.debug("FRED API key not configured, using fallback rates");
            return Optional.empty();
        }

        try {
            String url = buildUrl(seriesId);
            log.debug("Fetching rate from FRED: {}", seriesId);

            FredResponse response = restTemplate.getForObject(url, FredResponse.class);

            if (response != null && response.observations != null && !response.observations.isEmpty()) {
                // Get the most recent observation
                FredObservation latest = response.observations.get(response.observations.size() - 1);

                if (latest.value != null && !".".equals(latest.value)) {
                    BigDecimal rate = new BigDecimal(latest.value);
                    log.info("Fetched {} rate: {}% (date: {})", seriesId, rate, latest.date);
                    return Optional.of(rate);
                }
            }

            log.warn("No valid data returned from FRED for series: {}", seriesId);
            return Optional.empty();

        } catch (Exception e) {
            log.error("Error fetching rate from FRED API for {}: {}", seriesId, e.getMessage());
            return Optional.empty();
        }
    }

    private String buildUrl(String seriesId) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(30); // Get last 30 days of data

        return String.format(
            "%s?series_id=%s&api_key=%s&file_type=json&sort_order=desc&limit=1&observation_start=%s",
            FRED_BASE_URL,
            seriesId,
            apiKey,
            startDate.format(DateTimeFormatter.ISO_DATE)
        );
    }

    // Response DTOs for FRED API
    public static class FredResponse {
        public java.util.List<FredObservation> observations;
    }

    public static class FredObservation {
        public String date;
        public String value;
    }
}
