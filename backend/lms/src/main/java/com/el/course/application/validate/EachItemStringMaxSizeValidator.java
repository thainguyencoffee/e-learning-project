package com.el.course.application.validate;

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

        for (String benefit : benefits) {
            if (benefit.isBlank() || benefit.length() > max || benefit.length() < min) {
                return false;
            }
        }
        return true;
    }
}