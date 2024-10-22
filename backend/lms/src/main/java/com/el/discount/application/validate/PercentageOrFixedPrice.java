package com.el.discount.application.validate;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = PercentageOrFixedPriceValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface PercentageOrFixedPrice {

    String message() default "Percentage and fixed price cannot be null at the same time";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
