package com.el.order.domain;

import javax.money.MonetaryAmount;

public record ExchangeDetails(
        Long enrollmentId,
        Long courseId,
        MonetaryAmount additionalPrice

) {
}
