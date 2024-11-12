package com.el.discount.application.validate;

import com.el.common.ValidateMessages;
import com.el.discount.application.dto.DiscountDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PercentageOrFixedPriceValidator implements ConstraintValidator<PercentageOrFixedPrice, DiscountDTO> {

    @Override
    public boolean isValid(DiscountDTO discountDTO, ConstraintValidatorContext context) {
        // If the discountDTO is null, we consider it valid (null object pattern)
        if (discountDTO == null) {
            return true;
        }

        // Check the type of discount
        if (discountDTO.type() == null) {
            return true; // If type is null, we cannot validate further
        }

        // Determine if percentage and fixed price are null
        boolean percentageIsNull = discountDTO.percentage() == null;
        boolean maxValueIsNull = discountDTO.maxValue() == null;
        boolean fixedPriceIsNull = discountDTO.fixedPrice() == null;

        // Validate based on the discount type
        switch (discountDTO.type()) {
            case PERCENTAGE:
                if (percentageIsNull) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate(ValidateMessages.NOT_NULL)
                            .addPropertyNode("percentage")
                            .addConstraintViolation();
                    return false; // Validation failed
                }
                if (maxValueIsNull) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate(ValidateMessages.NOT_NULL)
                            .addPropertyNode("maxValue")
                            .addConstraintViolation();
                    return false; // Validation failed
                }
                break;

            case FIXED:
                if (fixedPriceIsNull) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate(ValidateMessages.NOT_NULL)
                            .addPropertyNode("fixedPrice")
                            .addConstraintViolation();
                    return false; // Validation failed
                }
                break;

            default:
                // Handle unexpected discount types if necessary
                break;
        }

        // If we reach here, validation passed
        return true;
    }
}
