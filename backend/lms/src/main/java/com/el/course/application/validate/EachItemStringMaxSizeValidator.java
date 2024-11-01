package com.el.course.application.validate;

import com.el.common.ValidateMessages;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Set;

public class EachItemStringMaxSizeValidator implements ConstraintValidator<EachItemStringMaxSize, Set<String>> {

    private int max;
    private int min;

    @Override
    public void initialize(EachItemStringMaxSize constraintAnnotation) {
        this.max = constraintAnnotation.max();  // Lấy giá trị max từ annotation
        this.min = constraintAnnotation.min();
    }

    @Override
    public boolean isValid(Set<String> benefits, ConstraintValidatorContext context) {
        if (benefits == null) {
            return true;
        }

        boolean isValid = true;
        for (String benefit : benefits) {
            if (benefit.isBlank()) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Value you provide must not be blank").addConstraintViolation();
                isValid = false;
            } else if (benefit.length() > max) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(ValidateMessages.MAX_LENGTH).addConstraintViolation();
                isValid = false;
            } else if (benefit.length() < min) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(ValidateMessages.MIN_LENGTH).addConstraintViolation();
                isValid = false;
            }
        }
        return isValid;
    }
}