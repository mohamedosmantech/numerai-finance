package com.fincalc.domain.validation.constraint;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom constraint annotation for validating tax filing status.
 * Ensures the status is one of the valid US tax filing statuses.
 */
@Documented
@Constraint(validatedBy = ValidFilingStatusValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidFilingStatus {

    String message() default "{validation.filing-status.invalid}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
