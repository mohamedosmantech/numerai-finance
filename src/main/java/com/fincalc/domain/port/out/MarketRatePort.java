package com.fincalc.domain.port.out;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

/**
 * Port for fetching current market rates from external sources.
 * Implementations may fetch from Federal Reserve, Freddie Mac, or other sources.
 */
public interface MarketRatePort {

    /**
     * Get current average 30-year fixed mortgage rate.
     */
    Optional<BigDecimal> getMortgageRate30Year();

    /**
     * Get current average 15-year fixed mortgage rate.
     */
    Optional<BigDecimal> getMortgageRate15Year();

    /**
     * Get current Federal Funds Rate (prime rate base).
     */
    Optional<BigDecimal> getFederalFundsRate();

    /**
     * Get current prime rate.
     */
    Optional<BigDecimal> getPrimeRate();

    /**
     * Get all available current rates.
     */
    Map<String, BigDecimal> getAllCurrentRates();

    /**
     * Get the date of last rate update.
     */
    String getLastUpdateDate();
}
