package com.fincalc.domain.model.config;

import java.util.Map;

/**
 * Localized message for tool responses and error messages.
 * Manageable via admin dashboard.
 *
 * Example usage:
 * - Tool response headers ("Loan Payment Summary" in multiple languages)
 * - Error messages
 * - Field labels
 * - Help text
 */
public record LocalizedMessage(
    String key,                    // Unique message key (e.g., "loan.payment.summary")
    String category,               // Category: "tool_response", "error", "field_label", "help"
    Map<String, String> translations  // Language code -> translated text
) {

    // Common language codes
    public static final String EN = "en";
    public static final String ES = "es";
    public static final String FR = "fr";
    public static final String DE = "de";
    public static final String ZH = "zh";
    public static final String AR = "ar";
    public static final String PT = "pt";
    public static final String JA = "ja";
    public static final String KO = "ko";
    public static final String HI = "hi";

    /**
     * Get translation for a language, falling back to English.
     */
    public String get(String languageCode) {
        String translation = translations.get(languageCode);
        if (translation != null) {
            return translation;
        }
        // Fallback to English
        return translations.getOrDefault(EN, key);
    }

    /**
     * Check if a translation exists for a language.
     */
    public boolean hasTranslation(String languageCode) {
        return translations.containsKey(languageCode);
    }
}
