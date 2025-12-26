package com.fincalc.adapter.in.web;

import com.fincalc.domain.model.config.Country;
import com.fincalc.domain.model.config.Currency;
import com.fincalc.domain.model.config.RateProvider;
import com.fincalc.domain.port.out.ConfigurationPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin API for managing system configuration.
 *
 * This API enables admin dashboard to:
 * - Manage supported countries and tax systems
 * - Manage supported currencies
 * - Configure external rate data providers
 *
 * In production, secure this with authentication/authorization.
 */
@RestController
@RequestMapping("/api/admin/config")
@RequiredArgsConstructor
@Tag(name = "Admin Configuration", description = "Manage countries, currencies, and rate providers")
public class AdminConfigController {

    private final ConfigurationPort configurationPort;

    // ==================== Countries ====================

    @GetMapping("/countries")
    @Operation(summary = "List all supported countries")
    public ResponseEntity<List<Country>> getAllCountries() {
        return ResponseEntity.ok(configurationPort.getAllCountries());
    }

    @GetMapping("/countries/{code}")
    @Operation(summary = "Get country by code")
    public ResponseEntity<Country> getCountry(@PathVariable String code) {
        return configurationPort.getCountry(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/countries")
    @Operation(summary = "Add or update a country configuration")
    public ResponseEntity<Map<String, String>> saveCountry(@RequestBody Country country) {
        configurationPort.saveCountry(country);
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Country " + country.code() + " saved successfully"
        ));
    }

    @DeleteMapping("/countries/{code}")
    @Operation(summary = "Delete a country configuration")
    public ResponseEntity<Map<String, String>> deleteCountry(@PathVariable String code) {
        configurationPort.deleteCountry(code);
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Country " + code + " deleted"
        ));
    }

    // ==================== Currencies ====================

    @GetMapping("/currencies")
    @Operation(summary = "List all supported currencies")
    public ResponseEntity<List<Currency>> getAllCurrencies() {
        return ResponseEntity.ok(configurationPort.getAllCurrencies());
    }

    @GetMapping("/currencies/{code}")
    @Operation(summary = "Get currency by code")
    public ResponseEntity<Currency> getCurrency(@PathVariable String code) {
        return configurationPort.getCurrency(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/currencies")
    @Operation(summary = "Add or update a currency configuration")
    public ResponseEntity<Map<String, String>> saveCurrency(@RequestBody Currency currency) {
        configurationPort.saveCurrency(currency);
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Currency " + currency.code() + " saved successfully"
        ));
    }

    // ==================== Rate Providers ====================

    @GetMapping("/rate-providers")
    @Operation(summary = "List all rate providers")
    public ResponseEntity<List<RateProvider>> getAllRateProviders() {
        return ResponseEntity.ok(configurationPort.getAllRateProviders());
    }

    @GetMapping("/rate-providers/enabled")
    @Operation(summary = "List only enabled rate providers")
    public ResponseEntity<List<RateProvider>> getEnabledRateProviders() {
        return ResponseEntity.ok(configurationPort.getEnabledRateProviders());
    }

    @GetMapping("/rate-providers/{id}")
    @Operation(summary = "Get rate provider by ID")
    public ResponseEntity<RateProvider> getRateProvider(@PathVariable String id) {
        return configurationPort.getRateProvider(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/rate-providers")
    @Operation(summary = "Add or update a rate provider")
    public ResponseEntity<Map<String, String>> saveRateProvider(@RequestBody RateProvider provider) {
        configurationPort.saveRateProvider(provider);
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Rate provider " + provider.id() + " saved successfully"
        ));
    }

    @PatchMapping("/rate-providers/{id}/enable")
    @Operation(summary = "Enable or disable a rate provider")
    public ResponseEntity<Map<String, String>> enableRateProvider(
            @PathVariable String id,
            @RequestParam boolean enabled) {
        configurationPort.enableRateProvider(id, enabled);
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Rate provider " + id + " " + (enabled ? "enabled" : "disabled")
        ));
    }

    // ==================== Cache Management ====================

    @PostMapping("/refresh-cache")
    @Operation(summary = "Refresh configuration cache after updates")
    public ResponseEntity<Map<String, String>> refreshCache() {
        configurationPort.refreshCache();
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Configuration cache refreshed"
        ));
    }

    // ==================== Summary ====================

    @GetMapping("/summary")
    @Operation(summary = "Get configuration summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        return ResponseEntity.ok(Map.of(
                "totalCountries", configurationPort.getAllCountries().size(),
                "totalCurrencies", configurationPort.getAllCurrencies().size(),
                "totalRateProviders", configurationPort.getAllRateProviders().size(),
                "enabledRateProviders", configurationPort.getEnabledRateProviders().size(),
                "supportedCountryCodes", configurationPort.getAllCountries().stream()
                        .map(Country::code).toList(),
                "supportedCurrencyCodes", configurationPort.getAllCurrencies().stream()
                        .map(Currency::code).toList()
        ));
    }
}
