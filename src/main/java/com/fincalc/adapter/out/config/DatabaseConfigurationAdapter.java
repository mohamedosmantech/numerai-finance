package com.fincalc.adapter.out.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fincalc.adapter.out.persistence.entity.*;
import com.fincalc.adapter.out.persistence.repository.*;
import com.fincalc.domain.model.config.*;
import com.fincalc.domain.model.config.Country.*;
import com.fincalc.domain.port.out.ConfigurationPort;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Database-backed configuration adapter that reads from PostgreSQL.
 * Data is managed via Liquibase migrations and admin dashboard.
 */
@Slf4j
@Component
@Primary
@RequiredArgsConstructor
public class DatabaseConfigurationAdapter implements ConfigurationPort {

    private final CountryRepository countryRepository;
    private final CurrencyRepository currencyRepository;
    private final RateProviderRepository rateProviderRepository;
    private final LocalizedMessageRepository messageRepository;
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        log.info("Loaded {} countries, {} currencies, {} providers, {} messages from database",
                countryRepository.count(), currencyRepository.count(),
                rateProviderRepository.count(), messageRepository.count());
    }

    @Override
    public Optional<Country> getCountry(String code) {
        return countryRepository.findById(code).map(this::toCountry);
    }

    @Override
    public List<Country> getAllCountries() {
        return countryRepository.findAll().stream()
                .map(this::toCountry)
                .collect(Collectors.toList());
    }

    @Override
    public void saveCountry(Country country) {
        // Read-only from database for now
        log.warn("saveCountry called but database is read-only via migrations");
    }

    @Override
    public Optional<Currency> getCurrency(String code) {
        return currencyRepository.findById(code).map(this::toCurrency);
    }

    @Override
    public List<Currency> getAllCurrencies() {
        return currencyRepository.findAll().stream()
                .map(this::toCurrency)
                .collect(Collectors.toList());
    }

    @Override
    public void saveCurrency(Currency currency) {
        log.warn("saveCurrency called but database is read-only via migrations");
    }

    @Override
    public Optional<RateProvider> getRateProvider(String id) {
        return rateProviderRepository.findById(id).map(this::toRateProvider);
    }

    @Override
    public List<RateProvider> getAllRateProviders() {
        return rateProviderRepository.findAll().stream()
                .map(this::toRateProvider)
                .collect(Collectors.toList());
    }

    @Override
    public void saveRateProvider(RateProvider provider) {
        log.warn("saveRateProvider called but database is read-only via migrations");
    }

    @Override
    public Optional<LocalizedMessage> getMessage(String key) {
        return messageRepository.findById(key).map(this::toMessage);
    }

    @Override
    public List<LocalizedMessage> getAllMessages() {
        return messageRepository.findAll().stream()
                .map(this::toMessage)
                .collect(Collectors.toList());
    }

    @Override
    public void saveMessage(LocalizedMessage message) {
        log.warn("saveMessage called but database is read-only via migrations");
    }

    @Override
    public Optional<ResponseTemplate> getTemplate(String id) {
        return Optional.empty(); // Templates not in DB yet
    }

    @Override
    public List<ResponseTemplate> getAllTemplates() {
        return List.of(); // Templates not in DB yet
    }

    @Override
    public void saveTemplate(ResponseTemplate template) {
        log.warn("saveTemplate called but database is read-only via migrations");
    }

    // Entity to Domain conversions
    private Currency toCurrency(CurrencyEntity e) {
        return new Currency(
                e.getCode(),
                e.getSymbol(),
                e.getName(),
                e.getDecimalPlaces(),
                e.isSymbolBefore(),
                e.getThousandsSeparator(),
                e.getDecimalSeparator()
        );
    }

    private Country toCountry(CountryEntity e) {
        List<Region> regions = parseRegions(e.getRegionsJson());
        TaxSystem taxSystem = parseTaxSystem(e.getTaxSystemJson());

        return new Country(
                e.getCode(),
                e.getName(),
                e.getCurrencyCode(),
                regions,
                taxSystem,
                e.isHasRegionalTax(),
                e.getRateSource()
        );
    }

    private RateProvider toRateProvider(RateProviderEntity e) {
        Map<String, String> seriesMapping = parseSeriesMapping(e.getSeriesMappingJson());

        return new RateProvider(
                e.getId(),
                e.getName(),
                e.getType(),
                e.getBaseUrl(),
                e.getApiKeyEnvVar(),
                e.getCacheDurationMinutes(),
                e.isEnabled(),
                seriesMapping
        );
    }

    private LocalizedMessage toMessage(LocalizedMessageEntity e) {
        Map<String, String> translations = parseTranslations(e.getTranslationsJson());

        return new LocalizedMessage(
                e.getMessageKey(),
                e.getCategory(),
                translations
        );
    }

    private List<Region> parseRegions(String json) {
        if (json == null || json.isBlank() || json.equals("[]")) {
            return List.of();
        }
        try {
            List<Map<String, Object>> list = objectMapper.readValue(json, new TypeReference<>() {});
            return list.stream()
                    .map(m -> new Region(
                            (String) m.get("code"),
                            (String) m.get("name"),
                            new TaxBracket[]{}
                    ))
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            log.warn("Failed to parse regions JSON: {}", ex.getMessage());
            return List.of();
        }
    }

    private TaxSystem parseTaxSystem(String json) {
        if (json == null || json.isBlank()) {
            return new TaxSystem("Unknown", new TaxBracket[]{}, new StandardDeduction[]{}, "2025");
        }
        try {
            Map<String, Object> map = objectMapper.readValue(json, new TypeReference<>() {});
            String authority = (String) map.getOrDefault("authority", "Unknown");
            String taxYear = (String) map.getOrDefault("taxYear", "2025");

            List<Map<String, Object>> bracketList = (List<Map<String, Object>>) map.getOrDefault("brackets", List.of());
            TaxBracket[] brackets = bracketList.stream()
                    .map(b -> new TaxBracket(
                            ((Number) b.get("min")).doubleValue(),
                            ((Number) b.get("max")).doubleValue(),
                            ((Number) b.get("rate")).doubleValue(),
                            ((Number) b.getOrDefault("baseTax", 0)).doubleValue()
                    ))
                    .toArray(TaxBracket[]::new);

            List<Map<String, Object>> deductionList = (List<Map<String, Object>>) map.getOrDefault("deductions", List.of());
            StandardDeduction[] deductions = deductionList.stream()
                    .map(d -> new StandardDeduction(
                            (String) d.get("type"),
                            ((Number) d.get("amount")).doubleValue()
                    ))
                    .toArray(StandardDeduction[]::new);

            return new TaxSystem(authority, brackets, deductions, taxYear);
        } catch (Exception ex) {
            log.warn("Failed to parse tax system JSON: {}", ex.getMessage());
            return new TaxSystem("Unknown", new TaxBracket[]{}, new StandardDeduction[]{}, "2025");
        }
    }

    private Map<String, String> parseSeriesMapping(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception ex) {
            log.warn("Failed to parse series mapping JSON: {}", ex.getMessage());
            return Map.of();
        }
    }

    private Map<String, String> parseTranslations(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception ex) {
            log.warn("Failed to parse translations JSON: {}", ex.getMessage());
            return Map.of();
        }
    }
}
