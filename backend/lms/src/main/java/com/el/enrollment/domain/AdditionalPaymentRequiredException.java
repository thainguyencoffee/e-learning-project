package com.el.enrollment.domain;

import lombok.Getter;

import javax.money.MonetaryAmount;

@Getter
public class AdditionalPaymentRequiredException extends Exception {

    private final MonetaryAmount priceAdditional;

    public AdditionalPaymentRequiredException(MonetaryAmount priceAdditional) {
        super("Additional payment required");
        this.priceAdditional = priceAdditional;
    }
}
