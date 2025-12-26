package com.fincalc.domain.validation.constraint;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Set;

/**
 * Validator implementation for @ValidFilingStatus constraint.
 * Validates that the filing status is a recognized US tax filing status.
 */
public class ValidFilingStatusValidator implements ConstraintValidator<ValidFilingStatus, String> {

    private static final Set<String> VALID_STATUSES = Set.of(
            "single",
            "married_joint",
            "married_separate",
            "head_of_household"
    );

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true; // Use @NotBlank for null/blank checks
        }

        return VALID_STATUSES.contains(value.toLowerCase());
    }
}
