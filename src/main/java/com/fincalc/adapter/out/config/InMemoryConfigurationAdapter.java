package com.fincalc.adapter.out.config;

import com.fincalc.domain.model.config.Country;
import com.fincalc.domain.model.config.Country.*;
import com.fincalc.domain.model.config.Currency;
import com.fincalc.domain.model.config.LocalizedMessage;
import com.fincalc.domain.model.config.RateProvider;
import com.fincalc.domain.model.config.ResponseTemplate;
import com.fincalc.domain.port.out.ConfigurationPort;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory configuration adapter with pre-loaded defaults.
 *
 * For production, replace with DatabaseConfigurationAdapter that
 * reads from a database table managed by admin dashboard.
 *
 * EXTENSIBILITY:
 * - Add new countries by calling saveCountry()
 * - Add new currencies by calling saveCurrency()
 * - Add new rate providers by calling saveRateProvider()
 * - All changes are immediately available via the API
 */
@Slf4j
@Component
public class InMemoryConfigurationAdapter implements ConfigurationPort {

    private final Map<String, Country> countries = new ConcurrentHashMap<>();
    private final Map<String, Currency> currencies = new ConcurrentHashMap<>();
    private final Map<String, RateProvider> rateProviders = new ConcurrentHashMap<>();
    private final Map<String, LocalizedMessage> messages = new ConcurrentHashMap<>();
    private final Map<String, ResponseTemplate> templates = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        loadDefaultCurrencies();
        loadDefaultCountries();
        loadDefaultRateProviders();
        loadDefaultMessages();
        loadDefaultTemplates();
        log.info("Loaded {} countries, {} currencies, {} providers, {} messages, {} templates",
                countries.size(), currencies.size(), rateProviders.size(),
                messages.size(), templates.size());
    }

    private void loadDefaultCurrencies() {
        // Major world currencies
        saveCurrency(new Currency("USD", "$", "US Dollar", 2, true, ",", "."));
        saveCurrency(new Currency("EUR", "€", "Euro", 2, false, ".", ","));
        saveCurrency(new Currency("GBP", "£", "British Pound", 2, true, ",", "."));
        saveCurrency(new Currency("JPY", "¥", "Japanese Yen", 0, true, ",", "."));
        saveCurrency(new Currency("CAD", "C$", "Canadian Dollar", 2, true, ",", "."));
        saveCurrency(new Currency("AUD", "A$", "Australian Dollar", 2, true, ",", "."));
        saveCurrency(new Currency("CHF", "Fr.", "Swiss Franc", 2, false, "'", "."));
        saveCurrency(new Currency("CNY", "¥", "Chinese Yuan", 2, true, ",", "."));
        saveCurrency(new Currency("INR", "₹", "Indian Rupee", 2, true, ",", "."));
        saveCurrency(new Currency("MXN", "$", "Mexican Peso", 2, true, ",", "."));
        saveCurrency(new Currency("BRL", "R$", "Brazilian Real", 2, true, ".", ","));
        saveCurrency(new Currency("AED", "د.إ", "UAE Dirham", 2, true, ",", "."));
        saveCurrency(new Currency("SAR", "﷼", "Saudi Riyal", 2, true, ",", "."));
        saveCurrency(new Currency("SGD", "S$", "Singapore Dollar", 2, true, ",", "."));
        saveCurrency(new Currency("KRW", "₩", "South Korean Won", 0, true, ",", "."));
    }

    private void loadDefaultCountries() {
        // United States - IRS 2025 tax brackets (Revenue Procedure 2024-40)
        saveCountry(new Country(
            "US", "United States", "USD",
            loadUSStates(),
            new TaxSystem("IRS", getUS2025FederalBrackets(), getUS2025Deductions(), "2025"),
            true, "FRED"
        ));

        // United Kingdom - HMRC 2025/26 tax brackets
        saveCountry(new Country(
            "GB", "United Kingdom", "GBP",
            List.of(
                new Region("ENG", "England", new TaxBracket[]{}),
                new Region("SCO", "Scotland", new TaxBracket[]{}),
                new Region("WAL", "Wales", new TaxBracket[]{}),
                new Region("NIR", "Northern Ireland", new TaxBracket[]{})
            ),
            new TaxSystem("HMRC", getUK2025TaxBrackets(), new StandardDeduction[]{
                new StandardDeduction("personal_allowance", 12570)
            }, "2025"),
            true, "BOE"
        ));

        // Canada - CRA 2025 tax brackets
        saveCountry(new Country(
            "CA", "Canada", "CAD",
            List.of(
                new Region("ON", "Ontario", new TaxBracket[]{}),
                new Region("QC", "Quebec", new TaxBracket[]{}),
                new Region("BC", "British Columbia", new TaxBracket[]{}),
                new Region("AB", "Alberta", new TaxBracket[]{})
            ),
            new TaxSystem("CRA", getCanada2025TaxBrackets(), new StandardDeduction[]{
                new StandardDeduction("basic_personal", 16129)
            }, "2025"),
            true, "BOC"
        ));

        // Germany - BZSt 2025 tax brackets
        saveCountry(new Country(
            "DE", "Germany", "EUR",
            List.of(), // Germany has uniform federal tax
            new TaxSystem("BZSt", getGermany2025TaxBrackets(), new StandardDeduction[]{
                new StandardDeduction("grundfreibetrag", 12084)
            }, "2025"),
            false, "ECB"
        ));

        // Australia - ATO 2024-25 fiscal year tax brackets
        saveCountry(new Country(
            "AU", "Australia", "AUD",
            List.of(), // Australia has uniform federal tax
            new TaxSystem("ATO", getAustralia2025TaxBrackets(), new StandardDeduction[]{}, "2024-25"),
            false, "RBA"
        ));
    }

    private void loadDefaultRateProviders() {
        // Federal Reserve Economic Data (US)
        saveRateProvider(new RateProvider(
            "FRED", "Federal Reserve Economic Data", "FRED",
            "https://api.stlouisfed.org/fred",
            "FRED_API_KEY",
            Map.of(
                RateProvider.MORTGAGE_30_YEAR, "MORTGAGE30US",
                RateProvider.MORTGAGE_15_YEAR, "MORTGAGE15US",
                RateProvider.PRIME_RATE, "DPRIME",
                RateProvider.CENTRAL_BANK_RATE, "FEDFUNDS"
            ),
            60, true
        ));

        // European Central Bank
        saveRateProvider(new RateProvider(
            "ECB", "European Central Bank", "ECB",
            "https://sdw-wsrest.ecb.europa.eu/service",
            "",
            Map.of(
                RateProvider.CENTRAL_BANK_RATE, "FM.D.U2.EUR.4F.KR.MRR_FR.LEV"
            ),
            60, true
        ));

        // Bank of England
        saveRateProvider(new RateProvider(
            "BOE", "Bank of England", "BOE",
            "https://www.bankofengland.co.uk/boeapps/iadb",
            "",
            Map.of(
                RateProvider.CENTRAL_BANK_RATE, "IUDBEDR"
            ),
            60, true
        ));
    }

    // US Federal Tax Brackets 2025 (IRS Revenue Procedure 2024-40)
    private TaxBracket[] getUS2025FederalBrackets() {
        return new TaxBracket[]{
            new TaxBracket(0, 11925, 0.10, 0),
            new TaxBracket(11925, 48475, 0.12, 1192.50),
            new TaxBracket(48475, 103350, 0.22, 5578.50),
            new TaxBracket(103350, 197300, 0.24, 17651),
            new TaxBracket(197300, 250525, 0.32, 40199),
            new TaxBracket(250525, 626350, 0.35, 57231),
            new TaxBracket(626350, Double.MAX_VALUE, 0.37, 188769.75)
        };
    }

    private StandardDeduction[] getUS2025Deductions() {
        return new StandardDeduction[]{
            new StandardDeduction("single", 15000),
            new StandardDeduction("married_filing_jointly", 30000),
            new StandardDeduction("married_filing_separately", 15000),
            new StandardDeduction("head_of_household", 22500)
        };
    }

    // UK Tax Brackets 2025/26 (HMRC)
    private TaxBracket[] getUK2025TaxBrackets() {
        return new TaxBracket[]{
            new TaxBracket(0, 12570, 0, 0),         // Personal allowance
            new TaxBracket(12570, 50270, 0.20, 0),  // Basic rate
            new TaxBracket(50270, 125140, 0.40, 7540), // Higher rate
            new TaxBracket(125140, Double.MAX_VALUE, 0.45, 37428) // Additional rate
        };
    }

    // Canada Federal Tax Brackets 2025 (CRA)
    private TaxBracket[] getCanada2025TaxBrackets() {
        return new TaxBracket[]{
            new TaxBracket(0, 57375, 0.15, 0),
            new TaxBracket(57375, 114750, 0.205, 8606.25),
            new TaxBracket(114750, 177882, 0.26, 20368.44),
            new TaxBracket(177882, 253414, 0.29, 36782.76),
            new TaxBracket(253414, Double.MAX_VALUE, 0.33, 58687.04)
        };
    }

    // Germany Tax Brackets 2025 (BZSt)
    private TaxBracket[] getGermany2025TaxBrackets() {
        return new TaxBracket[]{
            new TaxBracket(0, 12084, 0, 0),         // Grundfreibetrag
            new TaxBracket(12084, 17430, 0.14, 0),  // Zone 1
            new TaxBracket(17430, 68480, 0.24, 748), // Zone 2
            new TaxBracket(68480, 277825, 0.42, 13002), // Zone 3
            new TaxBracket(277825, Double.MAX_VALUE, 0.45, 100927) // Zone 4
        };
    }

    // Australia Tax Brackets 2024-25 fiscal year (ATO)
    private TaxBracket[] getAustralia2025TaxBrackets() {
        return new TaxBracket[]{
            new TaxBracket(0, 18200, 0, 0),
            new TaxBracket(18200, 45000, 0.16, 0),
            new TaxBracket(45000, 135000, 0.30, 4288),
            new TaxBracket(135000, 190000, 0.37, 31288),
            new TaxBracket(190000, Double.MAX_VALUE, 0.45, 51638)
        };
    }

    private List<Region> loadUSStates() {
        // Simplified - in production, load full state tax brackets
        return List.of(
            new Region("CA", "California", new TaxBracket[]{}),
            new Region("NY", "New York", new TaxBracket[]{}),
            new Region("TX", "Texas", new TaxBracket[]{}),  // No state income tax
            new Region("FL", "Florida", new TaxBracket[]{}), // No state income tax
            new Region("WA", "Washington", new TaxBracket[]{}),
            new Region("IL", "Illinois", new TaxBracket[]{}),
            new Region("PA", "Pennsylvania", new TaxBracket[]{}),
            new Region("OH", "Ohio", new TaxBracket[]{}),
            new Region("GA", "Georgia", new TaxBracket[]{}),
            new Region("NC", "North Carolina", new TaxBracket[]{})
        );
    }

    // ConfigurationPort implementation

    @Override
    public List<Country> getAllCountries() {
        return new ArrayList<>(countries.values());
    }

    @Override
    public Optional<Country> getCountry(String countryCode) {
        return Optional.ofNullable(countries.get(countryCode.toUpperCase()));
    }

    @Override
    public void saveCountry(Country country) {
        countries.put(country.code().toUpperCase(), country);
        log.info("Saved country configuration: {}", country.code());
    }

    @Override
    public void deleteCountry(String countryCode) {
        countries.remove(countryCode.toUpperCase());
    }

    @Override
    public List<Currency> getAllCurrencies() {
        return new ArrayList<>(currencies.values());
    }

    @Override
    public Optional<Currency> getCurrency(String currencyCode) {
        return Optional.ofNullable(currencies.get(currencyCode.toUpperCase()));
    }

    @Override
    public void saveCurrency(Currency currency) {
        currencies.put(currency.code().toUpperCase(), currency);
    }

    @Override
    public List<RateProvider> getAllRateProviders() {
        return new ArrayList<>(rateProviders.values());
    }

    @Override
    public Optional<RateProvider> getRateProvider(String providerId) {
        return Optional.ofNullable(rateProviders.get(providerId));
    }

    @Override
    public List<RateProvider> getEnabledRateProviders() {
        return rateProviders.values().stream()
                .filter(RateProvider::enabled)
                .toList();
    }

    @Override
    public void saveRateProvider(RateProvider provider) {
        rateProviders.put(provider.id(), provider);
    }

    @Override
    public void enableRateProvider(String providerId, boolean enabled) {
        RateProvider existing = rateProviders.get(providerId);
        if (existing != null) {
            rateProviders.put(providerId, new RateProvider(
                existing.id(), existing.name(), existing.type(),
                existing.baseUrl(), existing.apiKeyEnvVar(),
                existing.seriesMapping(), existing.cacheDurationMinutes(),
                enabled
            ));
        }
    }

    // ==================== Localized Messages ====================

    private void loadDefaultMessages() {
        // Loan tool messages
        saveMessage(new LocalizedMessage("loan.title", "tool_response", Map.of(
            "en", "Loan Payment Summary",
            "es", "Resumen del Pago del Préstamo",
            "fr", "Résumé du Paiement du Prêt",
            "de", "Zusammenfassung der Darlehenszahlung",
            "zh", "贷款支付摘要",
            "ar", "ملخص دفع القرض"
        )));
        saveMessage(new LocalizedMessage("loan.monthlyPayment", "field_label", Map.of(
            "en", "Monthly Payment",
            "es", "Pago Mensual",
            "fr", "Paiement Mensuel",
            "de", "Monatliche Zahlung",
            "zh", "月供",
            "ar", "الدفعة الشهرية"
        )));

        // Tax tool messages
        saveMessage(new LocalizedMessage("tax.title", "tool_response", Map.of(
            "en", "Tax Estimation",
            "es", "Estimación de Impuestos",
            "fr", "Estimation Fiscale",
            "de", "Steuerberechnung",
            "zh", "税务估算",
            "ar", "تقدير الضريبة"
        )));

        // Error messages
        saveMessage(new LocalizedMessage("error.invalidInput", "error", Map.of(
            "en", "Invalid input provided. Please check your values.",
            "es", "Entrada no válida. Por favor revise sus valores.",
            "fr", "Entrée invalide. Veuillez vérifier vos valeurs.",
            "de", "Ungültige Eingabe. Bitte überprüfen Sie Ihre Werte.",
            "zh", "输入无效。请检查您的值。",
            "ar", "إدخال غير صالح. يرجى التحقق من القيم الخاصة بك."
        )));
    }

    private void loadDefaultTemplates() {
        saveTemplate(ResponseTemplate.defaultForTool("calculate_loan_payment"));
        saveTemplate(ResponseTemplate.defaultForTool("calculate_compound_interest"));
        saveTemplate(ResponseTemplate.defaultForTool("estimate_taxes"));
        saveTemplate(ResponseTemplate.defaultForTool("get_current_rates"));
    }

    @Override
    public List<LocalizedMessage> getAllMessages() {
        return new ArrayList<>(messages.values());
    }

    @Override
    public List<LocalizedMessage> getMessagesByCategory(String category) {
        return messages.values().stream()
                .filter(m -> m.category().equals(category))
                .toList();
    }

    @Override
    public Optional<LocalizedMessage> getMessage(String key) {
        return Optional.ofNullable(messages.get(key));
    }

    @Override
    public void saveMessage(LocalizedMessage message) {
        messages.put(message.key(), message);
    }

    @Override
    public void deleteMessage(String key) {
        messages.remove(key);
    }

    @Override
    public String getTranslation(String key, String languageCode) {
        LocalizedMessage msg = messages.get(key);
        return msg != null ? msg.get(languageCode) : key;
    }

    // ==================== Response Templates ====================

    @Override
    public List<ResponseTemplate> getAllTemplates() {
        return new ArrayList<>(templates.values());
    }

    @Override
    public Optional<ResponseTemplate> getTemplate(String toolName, String countryCode, String languageCode) {
        // Try specific template first, then fall back to default
        String specificKey = toolName + ":" + countryCode + ":" + languageCode;
        ResponseTemplate template = templates.get(specificKey);
        if (template != null) return Optional.of(template);

        // Fall back to tool default
        return Optional.ofNullable(templates.get(toolName));
    }

    @Override
    public void saveTemplate(ResponseTemplate template) {
        String key = template.countryCode() != null
            ? template.toolName() + ":" + template.countryCode() + ":" + template.languageCode()
            : template.toolName();
        templates.put(key, template);
    }

    @Override
    public void refreshCache() {
        // In-memory adapter doesn't need cache refresh
        // Database adapter would clear and reload from DB
        log.info("Configuration cache refreshed");
    }
}
