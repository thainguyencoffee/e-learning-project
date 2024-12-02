package com.el.enrollment.application.dto;

import javax.money.MonetaryAmount;
import java.util.UUID;

public record ChangeCourseResponse(
        UUID orderId,
        Type type,
        Status status,
        MonetaryAmount priceAdditional
) {

    public enum Type {
        BASIC_CHANGE,
        PENDING_PAYMENT_ADDITIONAL,
    }

    public enum Status {
        SUCCESS,
        FAILED,
        PENDING
    }

    public static ChangeCourseResponse basicChange() {
        return new ChangeCourseResponse(null, Type.BASIC_CHANGE, Status.SUCCESS, null);
    }

    public static ChangeCourseResponse pendingPaymentAdditional(UUID orderId, MonetaryAmount priceAdditional) {
        return new ChangeCourseResponse(orderId, Type.PENDING_PAYMENT_ADDITIONAL, Status.PENDING, priceAdditional);
    }

}
