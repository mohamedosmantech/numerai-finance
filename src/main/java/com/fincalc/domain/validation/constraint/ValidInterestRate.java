package com.fincalc.domain.validation.constraint;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom constraint annotation for validating interest rates.
 * Ensures the rate is between 0 and the specified maximum (default 50%).
 */
@Documented
@Constraint(validatedBy = ValidInterestRateValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidInterestRate {

    String message() default "{validation.rate.range}";

    double min() default 0.0;

    double max() default 50.0;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
