package com.fincalc.domain.validation.constraint;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom constraint annotation for validating monetary amounts.
 * Ensures the value is a positive BigDecimal.
 */
@Documented
@Constraint(validatedBy = ValidMoneyValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidMoney {

    String message() default "{validation.money.positive}";

    boolean allowZero() default false;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
