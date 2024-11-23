package com.el.discount.web.dto;

import com.el.common.ValidateMessages;
import com.el.discount.web.validate.PercentageOrFixedPrice;
import com.el.discount.domain.Discount;
import com.el.discount.domain.Type;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import javax.money.MonetaryAmount;
import java.time.LocalDateTime;

@PercentageOrFixedPrice
public record DiscountDTO(
        @NotBlank(message = ValidateMessages.NOT_BLANK)
        @Size(min = 10, message = ValidateMessages.MIN_LENGTH)
        @Size(max = 50, message = ValidateMessages.MAX_LENGTH)
        String code,
        @NotNull(message = ValidateMessages.NOT_NULL)
        Type type,
        Double percentage,
        MonetaryAmount maxValue,
        MonetaryAmount fixedPrice,
        @NotNull(message = ValidateMessages.NOT_NULL)
        LocalDateTime startDate,
        @NotNull(message = ValidateMessages.NOT_NULL)
        LocalDateTime endDate,
        @NotNull(message = ValidateMessages.NOT_NULL)
        Integer maxUsage
) {

    public Discount toDiscount() {
        return new Discount(
                code,
                type,
                percentage,
                maxValue,
                fixedPrice,
                startDate,
                endDate,
                maxUsage
        );
    }
}
