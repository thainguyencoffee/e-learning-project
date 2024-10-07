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
    String message() default "Each benefit must be at most {max} characters long";

    int max() default 500;  // Giá trị mặc định là 500 ký tự

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
