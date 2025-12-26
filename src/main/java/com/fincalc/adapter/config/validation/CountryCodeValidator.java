package com.fincalc.adapter.config.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator for ISO 3166-1 alpha-2 country codes.
 */
public class CountryCodeValidator implements ConstraintValidator<ValidCountryCode, String> {

    private final InputSanitizer sanitizer = new InputSanitizer();

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true; // Let @NotBlank handle null/empty
        }
        return sanitizer.isValidCountryCode(value) && sanitizer.isSafeInput(value);
    }
}
