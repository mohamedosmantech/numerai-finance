package com.fincalc.adapter.out.rates;

import com.fincalc.domain.port.out.MarketRatePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Adapter for fetching market rates from external sources.
 * Uses FRED API as primary source with fallback to static rates.
 *
 * Data Sources:
 * - Federal Reserve (FRED): Mortgage rates, Fed funds rate, Prime rate
 * - Fallback: Static rates updated manually
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MarketRateAdapter implements MarketRatePort {

    private final FredApiClient fredApiClient;

    // Fallback rates (updated December 2024)
    // These are used when FRED API is unavailable
    private static final BigDecimal FALLBACK_MORTGAGE_30 = new BigDecimal("6.85");
    private static final BigDecimal FALLBACK_MORTGAGE_15 = new BigDecimal("6.10");
    private static final BigDecimal FALLBACK_FED_FUNDS = new BigDecimal("5.33");
    private static final BigDecimal FALLBACK_PRIME = new BigDecimal("8.50");

    private String lastUpdateDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE);

    @Override
    public Optional<BigDecimal> getMortgageRate30Year() {
        Optional<BigDecimal> rate = fredApiClient.getLatestRate(FredApiClient.MORTGAGE_30_YEAR);
        if (rate.isPresent()) {
            updateLastDate();
            return rate;
        }
        log.debug("Using fallback 30-year mortgage rate: {}", FALLBACK_MORTGAGE_30);
        return Optional.of(FALLBACK_MORTGAGE_30);
    }

    @Override
    public Optional<BigDecimal> getMortgageRate15Year() {
        Optional<BigDecimal> rate = fredApiClient.getLatestRate(FredApiClient.MORTGAGE_15_YEAR);
        if (rate.isPresent()) {
            updateLastDate();
            return rate;
        }
        log.debug("Using fallback 15-year mortgage rate: {}", FALLBACK_MORTGAGE_15);
        return Optional.of(FALLBACK_MORTGAGE_15);
    }

    @Override
    public Optional<BigDecimal> getFederalFundsRate() {
        Optional<BigDecimal> rate = fredApiClient.getLatestRate(FredApiClient.FEDERAL_FUNDS_RATE);
        if (rate.isPresent()) {
            updateLastDate();
            return rate;
        }
        log.debug("Using fallback federal funds rate: {}", FALLBACK_FED_FUNDS);
        return Optional.of(FALLBACK_FED_FUNDS);
    }

    @Override
    public Optional<BigDecimal> getPrimeRate() {
        Optional<BigDecimal> rate = fredApiClient.getLatestRate(FredApiClient.PRIME_RATE);
        if (rate.isPresent()) {
            updateLastDate();
            return rate;
        }
        log.debug("Using fallback prime rate: {}", FALLBACK_PRIME);
        return Optional.of(FALLBACK_PRIME);
    }

    @Override
    public Map<String, BigDecimal> getAllCurrentRates() {
        Map<String, BigDecimal> rates = new LinkedHashMap<>();

        getMortgageRate30Year().ifPresent(r -> rates.put("mortgage30Year", r));
        getMortgageRate15Year().ifPresent(r -> rates.put("mortgage15Year", r));
        getFederalFundsRate().ifPresent(r -> rates.put("federalFundsRate", r));
        getPrimeRate().ifPresent(r -> rates.put("primeRate", r));

        // Add some derived/reference rates
        rates.put("averageAutoLoan", new BigDecimal("7.50"));
        rates.put("averagePersonalLoan", new BigDecimal("12.00"));
        rates.put("averageCreditCard", new BigDecimal("24.00"));
        rates.put("highYieldSavings", new BigDecimal("4.50"));

        return rates;
    }

    @Override
    public String getLastUpdateDate() {
        return lastUpdateDate;
    }

    private void updateLastDate() {
        this.lastUpdateDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
    }
}
