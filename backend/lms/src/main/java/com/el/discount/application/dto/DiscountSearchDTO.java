package com.el.discount.application.dto;

import com.el.discount.domain.Type;

import javax.money.MonetaryAmount;
import java.time.LocalDateTime;

public record DiscountSearchDTO (
        String code,
        Type type,
        Double percentage,
        MonetaryAmount maxValue,
        MonetaryAmount fixedPrice,
        LocalDateTime startDate,
        LocalDateTime endDate,
        MonetaryAmount discountPrice
) {
}
