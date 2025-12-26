package com.fincalc.domain.port.out;

import com.fincalc.domain.model.config.Country;
import com.fincalc.domain.model.config.Currency;
import com.fincalc.domain.model.config.LocalizedMessage;
import com.fincalc.domain.model.config.RateProvider;
import com.fincalc.domain.model.config.ResponseTemplate;

import java.util.List;
import java.util.Optional;

/**
 * Port for managing system configuration.
 * Implementations can load from database, files, or external config service.
 *
 * This port enables admin dashboard to manage:
 * - Supported countries and their tax systems
 * - Supported currencies and formatting
 * - External rate data providers
 * - Localized messages for all languages
 * - Response templates for tool outputs
 */
public interface ConfigurationPort {

    // Country Management
    List<Country> getAllCountries();
    Optional<Country> getCountry(String countryCode);
    void saveCountry(Country country);
    void deleteCountry(String countryCode);

    // Currency Management
    List<Currency> getAllCurrencies();
    Optional<Currency> getCurrency(String currencyCode);
    void saveCurrency(Currency currency);

    // Rate Provider Management
    List<RateProvider> getAllRateProviders();
    Optional<RateProvider> getRateProvider(String providerId);
    List<RateProvider> getEnabledRateProviders();
    void saveRateProvider(RateProvider provider);
    void enableRateProvider(String providerId, boolean enabled);

    // Localized Message Management
    List<LocalizedMessage> getAllMessages();
    List<LocalizedMessage> getMessagesByCategory(String category);
    Optional<LocalizedMessage> getMessage(String key);
    void saveMessage(LocalizedMessage message);
    void deleteMessage(String key);
    String getTranslation(String key, String languageCode);

    // Response Template Management
    List<ResponseTemplate> getAllTemplates();
    Optional<ResponseTemplate> getTemplate(String toolName, String countryCode, String languageCode);
    void saveTemplate(ResponseTemplate template);

    // Refresh configuration cache (called after admin updates)
    void refreshCache();
}
