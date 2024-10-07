package com.el.course.application.validate;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Set;

public class EachItemStringMaxSizeValidator implements ConstraintValidator<EachItemStringMaxSize, Set<String>> {

    private int max;

    @Override
    public void initialize(EachItemStringMaxSize constraintAnnotation) {
        this.max = constraintAnnotation.max();  // Lấy giá trị max từ annotation
    }

    @Override
    public boolean isValid(Set<String> benefits, ConstraintValidatorContext context) {
        if (benefits == null) {
            return true; // Không cần validate nếu Set null
        }

        for (String benefit : benefits) {
            if (benefit == null || benefit.length() > max) {
                return false;  // Trả về false nếu có phần tử không hợp lệ
            }
        }
        return true;  // Tất cả các phần tử đều hợp lệ
    }
}