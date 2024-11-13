package com.el.course.web.validate;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Constraint(validatedBy = QuestionSubmitValidator.class)
public @interface QuestionSubmitConstraint {

    String message() default "Invalid question submit";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
