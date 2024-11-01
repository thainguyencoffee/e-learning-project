package com.el.order.application.dto;

import com.el.common.ValidateMessages;
import jakarta.validation.constraints.NotNull;

public record OrderItemDTO(
        @NotNull(message = ValidateMessages.NOT_NULL)
        Long id
) {
}
