package com.el.course.web.dto;

import jakarta.validation.constraints.NotBlank;

public record ApplyDiscountDTO(
        @NotBlank(message = "Discount code is required")
        String code
) {
}
