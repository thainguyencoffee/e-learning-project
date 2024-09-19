package com.elearning.discount.application;

import com.elearning.common.util.ResourceNotFoundException;
import com.elearning.discount.domain.Discount;
import com.elearning.discount.domain.DiscountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.money.MonetaryAmount;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiscountService {
    private final DiscountRepository discountRepository;

    public Discount findById(Long id) {
        return discountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Discount.class, id));
    }

    public MonetaryAmount calculateDiscountForCourse(Long discountId, MonetaryAmount originalPrice) {
        var discount = findById(discountId);
        if (discount.isValid()) {
            return discount.calculateDiscount(originalPrice);
        } else {
            log.error("Discount with id {} is not valid", discountId);
            throw new DiscountInvalidDateException();
        }
    }

}
