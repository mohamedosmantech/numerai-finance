package com.fincalc.domain.validation.constraint;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator implementation for @ValidLoanTerm constraint.
 * Validates that loan/investment terms are within acceptable bounds.
 */
public class ValidLoanTermValidator implements ConstraintValidator<ValidLoanTerm, Integer> {

    private int min;
    private int max;

    @Override
    public void initialize(ValidLoanTerm constraintAnnotation) {
        this.min = constraintAnnotation.min();
        this.max = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Use @NotNull for null checks
        }

        return value >= min && value <= max;
    }
}
