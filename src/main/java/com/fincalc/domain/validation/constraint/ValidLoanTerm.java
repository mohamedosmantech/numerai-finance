package com.fincalc.domain.validation.constraint;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom constraint annotation for validating loan/investment term in years.
 * Ensures the term is within acceptable bounds.
 */
@Documented
@Constraint(validatedBy = ValidLoanTermValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidLoanTerm {

    String message() default "{validation.term.range}";

    int min() default 1;

    int max() default 50;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
