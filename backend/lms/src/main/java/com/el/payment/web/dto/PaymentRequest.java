package com.el.payment.web.dto;

import com.el.payment.domain.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import javax.money.MonetaryAmount;
import java.util.UUID;

public record PaymentRequest(
        @NotNull(message = "Order ID must not be null.")
        UUID orderId,
        @NotNull(message = "Amount must not be null.")
        MonetaryAmount amount,
        @NotNull(message = "Payment method must not be null.")
        PaymentMethod paymentMethod,
        @NotBlank(message = "Token must not be blank.")
        String token
) {
}