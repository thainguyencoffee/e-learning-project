package com.el.order.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record OrderRequestDTO(
        @NotEmpty(message = "Items can't be empty")
        @Valid
        Set<OrderItemDTO> items,
        String discountCode
) {

}
