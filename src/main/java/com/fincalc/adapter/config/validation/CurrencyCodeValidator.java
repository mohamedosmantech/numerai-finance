package com.fincalc.adapter.config.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator for ISO 4217 currency codes.
 */
public class CurrencyCodeValidator implements ConstraintValidator<ValidCurrencyCode, String> {

    private final InputSanitizer sanitizer = new InputSanitizer();

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true; // Let @NotBlank handle null/empty
        }
        return sanitizer.isValidCurrencyCode(value) && sanitizer.isSafeInput(value);
    }
}
