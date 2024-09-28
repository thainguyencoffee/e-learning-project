package com.elearning.course.web;

import jakarta.validation.constraints.NotNull;

import javax.money.MonetaryAmount;

public record MonetaryPriceDTO(
        @NotNull(message = "Price is required")
        MonetaryAmount price
) {
}
