package com.fincalc.adapter.in.web;

import com.fincalc.domain.port.out.MarketRatePort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * REST controller for current market rates.
 * Provides real-time rates from Federal Reserve and other sources.
 */
@RestController
@RequestMapping("/api/rates")
@RequiredArgsConstructor
@Tag(name = "Market Rates", description = "Current interest rates from Federal Reserve and market sources")
public class RatesController {

    private final MarketRatePort marketRatePort;

    @GetMapping
    @Operation(summary = "Get all current market rates",
               description = "Returns current mortgage rates, federal funds rate, prime rate, and more")
    public ResponseEntity<Map<String, Object>> getAllRates() {
        Map<String, Object> response = new LinkedHashMap<>();

        response.put("lastUpdated", marketRatePort.getLastUpdateDate());
        response.put("source", "Federal Reserve Economic Data (FRED)");
        response.put("rates", marketRatePort.getAllCurrentRates());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/mortgage")
    @Operation(summary = "Get current mortgage rates",
               description = "Returns 30-year and 15-year fixed mortgage rates")
    public ResponseEntity<Map<String, Object>> getMortgageRates() {
        Map<String, Object> response = new LinkedHashMap<>();

        response.put("lastUpdated", marketRatePort.getLastUpdateDate());
        response.put("source", "Freddie Mac Primary Mortgage Market Survey");

        Map<String, BigDecimal> rates = new LinkedHashMap<>();
        marketRatePort.getMortgageRate30Year().ifPresent(r -> rates.put("thirtyYearFixed", r));
        marketRatePort.getMortgageRate15Year().ifPresent(r -> rates.put("fifteenYearFixed", r));

        response.put("rates", rates);
        response.put("note", "Rates are national averages and may vary by lender, credit score, and location.");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/federal")
    @Operation(summary = "Get Federal Reserve rates",
               description = "Returns federal funds rate and prime rate")
    public ResponseEntity<Map<String, Object>> getFederalRates() {
        Map<String, Object> response = new LinkedHashMap<>();

        response.put("lastUpdated", marketRatePort.getLastUpdateDate());
        response.put("source", "Federal Reserve");

        Map<String, BigDecimal> rates = new LinkedHashMap<>();
        marketRatePort.getFederalFundsRate().ifPresent(r -> rates.put("federalFundsRate", r));
        marketRatePort.getPrimeRate().ifPresent(r -> rates.put("primeRate", r));

        response.put("rates", rates);

        return ResponseEntity.ok(response);
    }
}
