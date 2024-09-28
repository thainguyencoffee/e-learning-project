package com.elearning.discount.application;

import com.elearning.common.exception.ResourceNotFoundException;
import com.elearning.discount.domain.Discount;
import com.elearning.discount.domain.DiscountRepository;
import org.springframework.stereotype.Service;

@Service
public class DiscountQueryService {

    private final DiscountRepository discountRepository;

    public DiscountQueryService(DiscountRepository discountRepository) {
        this.discountRepository = discountRepository;
    }

    public Discount findById(Long id) {
        return discountRepository.findById(id)
                .orElseThrow(ResourceNotFoundException::new);
    }

}
