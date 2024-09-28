package com.elearning.discount.application;

import com.elearning.discount.domain.Discount;

import javax.money.MonetaryAmount;

public interface DiscountService {

    Discount findById(Long id);

    MonetaryAmount calculateDiscount(Long discountId, MonetaryAmount originalPrice);

}
