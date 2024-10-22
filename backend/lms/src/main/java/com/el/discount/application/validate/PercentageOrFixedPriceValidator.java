package com.el.discount.application.validate;

import com.el.discount.application.dto.DiscountDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PercentageOrFixedPriceValidator implements ConstraintValidator<PercentageOrFixedPrice, DiscountDTO> {

    @Override
    public boolean isValid(DiscountDTO discountDTO, ConstraintValidatorContext constraintValidatorContext) {
        return !(discountDTO.percentage() == null && discountDTO.fixedPrice() == null);
    }
}
