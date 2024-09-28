package com.elearning.discount.application;

import com.elearning.common.UseCase;

import javax.money.MonetaryAmount;

@UseCase
public class CalculateDiscountUseCase {

    private final DiscountQueryService discountQueryService;

    public CalculateDiscountUseCase(DiscountQueryService discountQueryService) {
        this.discountQueryService = discountQueryService;
    }

    public MonetaryAmount execute(Long discountId, MonetaryAmount originalPrice) {
        var discount = discountQueryService.findById(discountId);
        return discount.calculateDiscount(originalPrice);
    }

}
