package com.fincalc.domain.validation.constraint;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigDecimal;

/**
 * Validator implementation for @ValidMoney constraint.
 * Validates that monetary amounts are positive (or zero if allowed).
 */
public class ValidMoneyValidator implements ConstraintValidator<ValidMoney, BigDecimal> {

    private boolean allowZero;

    @Override
    public void initialize(ValidMoney constraintAnnotation) {
        this.allowZero = constraintAnnotation.allowZero();
    }

    @Override
    public boolean isValid(BigDecimal value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Use @NotNull for null checks
        }

        if (allowZero) {
            return value.compareTo(BigDecimal.ZERO) >= 0;
        }
        return value.compareTo(BigDecimal.ZERO) > 0;
    }
}
