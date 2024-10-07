package com.el.order.application.dto;

import jakarta.validation.constraints.NotNull;

public record OrderItemDTO(
        @NotNull(message = "Course ID can't be null")
        Long id
) {
}
