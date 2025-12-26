package com.fincalc.adapter.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.*;

/**
 * Request-scoped component that extracts and provides context from ChatGPT/OpenAI headers.
 *
 * Known ChatGPT/OpenAI headers:
 * - X-OpenAI-Country: User's country code (e.g., "US", "GB")
 * - X-OpenAI-Language: User's language preference (e.g., "en", "es")
 * - X-OpenAI-User-Id: Hashed user identifier
 * - X-OpenAI-Conversation-Id: Conversation identifier
 * - X-OpenAI-Ephemeral-User-Id: Ephemeral user ID
 * - X-Request-Id: Request tracking ID
 * - Accept-Language: Standard HTTP language header
 * - CF-IPCountry: Cloudflare's country detection
 * - X-Forwarded-For: Original client IP
 * - User-Agent: Client identification
 *
 * Additionally captures any header starting with X-OpenAI- or X-ChatGPT-
 */
@Slf4j
@Component
@RequestScope
@Getter
public class ChatGptRequestContext {

    // Core fields
    private final String countryCode;
    private final String languageCode;
    private final Locale locale;
    private final String requestId;
    private final String userId;
    private final String conversationId;
    private final String ephemeralUserId;
    private final boolean fromChatGpt;
    private final String userAgent;
    private final String clientIp;
    private final String timezone;
    private final String currency;

    // All OpenAI/ChatGPT headers
    private final Map<String, String> openAiHeaders;
    // All headers for debugging/logging
    private final Map<String, String> allHeaders;
    // All request parameters
    private final Map<String, String> allParams;
    // Request path and method
    private final String requestPath;
    private final String requestMethod;

    public ChatGptRequestContext(HttpServletRequest request) {
        this.allHeaders = extractAllHeaders(request);
        this.openAiHeaders = extractOpenAiHeaders(request);
        this.allParams = extractAllParams(request);
        this.requestPath = request.getRequestURI();
        this.requestMethod = request.getMethod();

        // Log all headers and params for debugging in development
        if (!openAiHeaders.isEmpty()) {
            log.info("OpenAI/ChatGPT headers detected: {}", openAiHeaders);
        }
        if (!allParams.isEmpty()) {
            log.debug("Request parameters: {}", allParams);
        }

        // Extract OpenAI-specific headers
        this.countryCode = extractHeader(request, "X-OpenAI-Country", "X-Country", "CF-IPCountry")
                .orElse("US");

        this.languageCode = extractLanguage(request);
        this.locale = Locale.forLanguageTag(this.languageCode);

        this.requestId = extractHeader(request, "X-Request-Id", "X-OpenAI-Request-Id")
                .orElse(null);

        this.userId = extractHeader(request, "X-OpenAI-User-Id")
                .orElse(null);

        this.conversationId = extractHeader(request, "X-OpenAI-Conversation-Id", "X-ChatGPT-Conversation-Id")
                .orElse(null);

        this.ephemeralUserId = extractHeader(request, "X-OpenAI-Ephemeral-User-Id")
                .orElse(null);

        this.timezone = extractHeader(request, "X-OpenAI-Timezone", "X-Timezone", "TZ")
                .orElse(null);

        this.currency = extractHeader(request, "X-OpenAI-Currency", "X-Currency")
                .orElse(getCurrencyForCountry(countryCode));

        this.clientIp = extractHeader(request, "X-Forwarded-For", "X-Real-IP")
                .map(ip -> ip.split(",")[0].trim())
                .orElse(request.getRemoteAddr());

        this.userAgent = request.getHeader("User-Agent");

        // Detect if request is coming from ChatGPT
        this.fromChatGpt = (userAgent != null &&
                (userAgent.contains("ChatGPT") || userAgent.contains("OpenAI")))
                || !openAiHeaders.isEmpty();
    }

    private Map<String, String> extractAllHeaders(HttpServletRequest request) {
        Map<String, String> headers = new LinkedHashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            headers.put(name, request.getHeader(name));
        }
        return Collections.unmodifiableMap(headers);
    }

    private Map<String, String> extractOpenAiHeaders(HttpServletRequest request) {
        Map<String, String> headers = new LinkedHashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            String lowerName = name.toLowerCase();
            if (lowerName.startsWith("x-openai-") || lowerName.startsWith("x-chatgpt-")) {
                headers.put(name, request.getHeader(name));
            }
        }
        return Collections.unmodifiableMap(headers);
    }

    private Map<String, String> extractAllParams(HttpServletRequest request) {
        Map<String, String> params = new LinkedHashMap<>();
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String name = paramNames.nextElement();
            String[] values = request.getParameterValues(name);
            if (values != null && values.length > 0) {
                // Join multiple values with comma
                params.put(name, String.join(",", values));
            }
        }
        return Collections.unmodifiableMap(params);
    }

    private Optional<String> extractHeader(HttpServletRequest request, String... headerNames) {
        for (String name : headerNames) {
            String value = request.getHeader(name);
            if (value != null && !value.isBlank()) {
                return Optional.of(value.trim());
            }
        }
        return Optional.empty();
    }

    private String extractLanguage(HttpServletRequest request) {
        // First try OpenAI-specific language header
        String openAiLang = request.getHeader("X-OpenAI-Language");
        if (openAiLang != null && !openAiLang.isBlank()) {
            return normalizeLanguageCode(openAiLang);
        }

        // Fall back to Accept-Language header
        String acceptLang = request.getHeader("Accept-Language");
        if (acceptLang != null && !acceptLang.isBlank()) {
            // Parse first language from Accept-Language (e.g., "en-US,en;q=0.9")
            String firstLang = acceptLang.split(",")[0].split(";")[0].trim();
            return normalizeLanguageCode(firstLang);
        }

        return "en"; // Default to English
    }

    private String normalizeLanguageCode(String lang) {
        if (lang == null || lang.isBlank()) {
            return "en";
        }
        // Extract primary language subtag (e.g., "en-US" -> "en")
        String primary = lang.split("[-_]")[0].toLowerCase();
        return primary.length() >= 2 ? primary.substring(0, 2) : "en";
    }

    private String getCurrencyForCountry(String country) {
        return switch (country.toUpperCase()) {
            case "US" -> "USD";
            case "GB", "UK" -> "GBP";
            case "CA" -> "CAD";
            case "AU" -> "AUD";
            case "DE", "FR", "IT", "ES", "NL", "BE", "AT", "IE", "PT", "FI" -> "EUR";
            case "JP" -> "JPY";
            case "CH" -> "CHF";
            case "IN" -> "INR";
            case "CN" -> "CNY";
            case "MX" -> "MXN";
            case "BR" -> "BRL";
            default -> "USD";
        };
    }

    /**
     * Get the country code in uppercase (ISO 3166-1 alpha-2).
     */
    public String getCountryCodeUpperCase() {
        return countryCode.toUpperCase();
    }

    /**
     * Check if a specific country was detected.
     */
    public boolean isCountry(String code) {
        return countryCode.equalsIgnoreCase(code);
    }

    /**
     * Check if a specific language was detected.
     */
    public boolean isLanguage(String code) {
        return languageCode.equalsIgnoreCase(code);
    }

    /**
     * Get a specific OpenAI header by name.
     */
    public Optional<String> getOpenAiHeader(String name) {
        return Optional.ofNullable(openAiHeaders.get(name));
    }

    /**
     * Get any header by name.
     */
    public Optional<String> getHeader(String name) {
        return Optional.ofNullable(allHeaders.get(name));
    }

    /**
     * Check if a specific header exists.
     */
    public boolean hasHeader(String name) {
        return allHeaders.containsKey(name);
    }

    /**
     * Get a specific request parameter by name.
     */
    public Optional<String> getParam(String name) {
        return Optional.ofNullable(allParams.get(name));
    }

    /**
     * Check if a specific parameter exists.
     */
    public boolean hasParam(String name) {
        return allParams.containsKey(name);
    }

    /**
     * Get a summary of the request context for logging.
     */
    public Map<String, Object> toLogContext() {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("country", countryCode);
        context.put("language", languageCode);
        context.put("currency", currency);
        context.put("fromChatGpt", fromChatGpt);
        if (requestId != null) context.put("requestId", requestId);
        if (userId != null) context.put("userId", userId);
        if (conversationId != null) context.put("conversationId", conversationId);
        if (timezone != null) context.put("timezone", timezone);
        if (!openAiHeaders.isEmpty()) context.put("openAiHeaders", openAiHeaders);
        if (!allParams.isEmpty()) context.put("params", allParams);
        return context;
    }
}
