package com.fincalc.adapter.config.validation;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Utility class for sanitizing and validating input to prevent SQL injection,
 * XSS attacks, and other security vulnerabilities.
 */
@Component
public class InputSanitizer {

    // Pattern for detecting SQL injection attempts
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
            "(?i)(--|;|'|\"|\\\\/\\\\*|\\\\*\\\\/|\\bUNION\\b|\\bSELECT\\b|\\bINSERT\\b|" +
            "\\bDELETE\\b|\\bUPDATE\\b|\\bDROP\\b|\\bEXEC\\b|\\bXP_\\b|\\bSP_\\b|" +
            "\\bOR\\s+\\d+=\\d+|\\bAND\\s+\\d+=\\d+|1=1|\\bOR\\s+'\\w+'='\\w+')"
    );

    // Pattern for detecting XSS attempts
    private static final Pattern XSS_PATTERN = Pattern.compile(
            "(?i)(<script|</script|javascript:|onerror=|onload=|onclick=|onmouseover=|" +
            "onfocus=|onblur=|<iframe|<object|<embed|<form|<input|<img[^>]+onerror)"
    );

    // Valid country code pattern (ISO 3166-1 alpha-2)
    private static final Pattern COUNTRY_CODE_PATTERN = Pattern.compile("^[A-Z]{2}$");

    // Valid currency code pattern (ISO 4217)
    private static final Pattern CURRENCY_CODE_PATTERN = Pattern.compile("^[A-Z]{3}$");

    // Valid identifier pattern (alphanumeric with underscores and hyphens)
    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_-]{0,49}$");

    // Valid name pattern (letters, spaces, hyphens, apostrophes)
    private static final Pattern NAME_PATTERN = Pattern.compile("^[\\p{L}\\s'-]{1,100}$");

    /**
     * Check if input contains potential SQL injection patterns.
     */
    public boolean containsSqlInjection(String input) {
        if (input == null || input.isBlank()) {
            return false;
        }
        return SQL_INJECTION_PATTERN.matcher(input).find();
    }

    /**
     * Check if input contains potential XSS patterns.
     */
    public boolean containsXss(String input) {
        if (input == null || input.isBlank()) {
            return false;
        }
        return XSS_PATTERN.matcher(input).find();
    }

    /**
     * Validate that input is safe (no SQL injection or XSS).
     */
    public boolean isSafeInput(String input) {
        return !containsSqlInjection(input) && !containsXss(input);
    }

    /**
     * Validate a country code (ISO 3166-1 alpha-2).
     */
    public boolean isValidCountryCode(String code) {
        if (code == null || code.isBlank()) {
            return false;
        }
        return COUNTRY_CODE_PATTERN.matcher(code.toUpperCase()).matches();
    }

    /**
     * Validate a currency code (ISO 4217).
     */
    public boolean isValidCurrencyCode(String code) {
        if (code == null || code.isBlank()) {
            return false;
        }
        return CURRENCY_CODE_PATTERN.matcher(code.toUpperCase()).matches();
    }

    /**
     * Validate an identifier (e.g., provider ID).
     */
    public boolean isValidIdentifier(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return false;
        }
        return IDENTIFIER_PATTERN.matcher(identifier).matches() && isSafeInput(identifier);
    }

    /**
     * Validate a name field.
     */
    public boolean isValidName(String name) {
        if (name == null || name.isBlank()) {
            return false;
        }
        return NAME_PATTERN.matcher(name).matches() && isSafeInput(name);
    }

    /**
     * Sanitize input by escaping potentially dangerous characters.
     */
    public String sanitize(String input) {
        if (input == null) {
            return null;
        }
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;")
                .replace("/", "&#x2F;");
    }

    /**
     * Normalize and sanitize a code (uppercase, trim, validate).
     */
    public String normalizeCode(String code) {
        if (code == null) {
            return null;
        }
        return code.trim().toUpperCase();
    }
}
