package com.elearning.course.web.dto;

import jakarta.validation.constraints.NotNull;

import javax.money.MonetaryAmount;

public record UpdatePriceDTO(
        @NotNull(message = "Price is required")
        MonetaryAmount price
) {
}
