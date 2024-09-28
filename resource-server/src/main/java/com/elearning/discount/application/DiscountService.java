package com.elearning.discount.application;

import com.elearning.discount.domain.Discount;

import javax.money.MonetaryAmount;

public interface DiscountService {

    Discount findById(Long id);

    Discount findByCode(String code);

    MonetaryAmount calculateDiscount(String code, MonetaryAmount originalPrice);

}
