package com.fincalc.adapter.config.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Validates that a string is a valid ISO 3166-1 alpha-2 country code.
 */
@Documented
@Constraint(validatedBy = CountryCodeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCountryCode {
    String message() default "Invalid country code. Must be a 2-letter ISO 3166-1 alpha-2 code";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
