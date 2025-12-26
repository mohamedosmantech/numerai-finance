package com.fincalc.adapter.config.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator for the @SafeInput annotation.
 * Checks for SQL injection and XSS patterns.
 */
public class SafeInputValidator implements ConstraintValidator<SafeInput, String> {

    private final InputSanitizer sanitizer = new InputSanitizer();

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true; // Let @NotBlank handle null/empty validation
        }
        return sanitizer.isSafeInput(value);
    }
}
