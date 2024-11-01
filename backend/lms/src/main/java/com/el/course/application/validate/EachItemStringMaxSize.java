package com.el.course.application.validate;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = EachItemStringMaxSizeValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface EachItemStringMaxSize {
    String message() default "Each item must be at most {max} characters long and at least {min} characters";

    int max() default 500;
    int min() default 25;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
