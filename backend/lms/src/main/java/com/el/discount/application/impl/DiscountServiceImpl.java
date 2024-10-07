package com.el.discount.application.impl;

import com.el.common.exception.ResourceNotFoundException;
import com.el.discount.application.DiscountService;
import com.el.discount.domain.Discount;
import com.el.discount.domain.DiscountRepository;
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
    public Discount findByCode(String code) {
        return discountRepository.findByCode(code)
                .orElseThrow(ResourceNotFoundException::new);
    }

    @Override
    public MonetaryAmount calculateDiscount(String code, MonetaryAmount originalPrice) {
        var discount = findByCode(code);
        return discount.calculateDiscount(originalPrice);
    }
}
