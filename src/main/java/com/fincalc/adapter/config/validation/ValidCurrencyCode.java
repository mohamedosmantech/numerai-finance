package com.fincalc.adapter.config.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Validates that a string is a valid ISO 4217 currency code.
 */
@Documented
@Constraint(validatedBy = CurrencyCodeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCurrencyCode {
    String message() default "Invalid currency code. Must be a 3-letter ISO 4217 code";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
