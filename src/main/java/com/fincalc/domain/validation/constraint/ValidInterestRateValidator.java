package com.fincalc.domain.validation.constraint;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigDecimal;

/**
 * Validator implementation for @ValidInterestRate constraint.
 * Validates that interest rates are within acceptable bounds.
 */
public class ValidInterestRateValidator implements ConstraintValidator<ValidInterestRate, BigDecimal> {

    private double min;
    private double max;

    @Override
    public void initialize(ValidInterestRate constraintAnnotation) {
        this.min = constraintAnnotation.min();
        this.max = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(BigDecimal value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Use @NotNull for null checks
        }

        double rate = value.doubleValue();
        return rate >= min && rate <= max;
    }
}
