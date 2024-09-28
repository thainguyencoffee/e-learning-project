package com.elearning.discount.application.impl;

import com.elearning.common.exception.ResourceNotFoundException;
import com.elearning.discount.application.DiscountService;
import com.elearning.discount.domain.Discount;
import com.elearning.discount.domain.DiscountRepository;
import org.springframework.stereotype.Service;

import javax.money.MonetaryAmount;

@Service
public class DiscountServiceImpl implements DiscountService {

    private final DiscountRepository discountRepository;

    public DiscountServiceImpl(DiscountRepository discountRepository) {
        this.discountRepository = discountRepository;
    }

    @Override
    public Discount findById(Long id) {
        return discountRepository.findById(id)
                .orElseThrow(ResourceNotFoundException::new);
    }

    @Override
    public MonetaryAmount calculateDiscount(Long discountId, MonetaryAmount originalPrice) {
        var discount = findById(discountId);
        return discount.calculateDiscount(originalPrice);
    }
}
