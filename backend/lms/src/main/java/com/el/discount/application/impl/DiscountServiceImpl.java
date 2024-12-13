package com.el.discount.application.impl;

import com.el.common.exception.InputInvalidException;
import com.el.common.exception.ResourceNotFoundException;
import com.el.discount.application.DiscountService;
import com.el.discount.web.dto.DiscountDTO;
import com.el.discount.application.dto.DiscountSearchDTO;
import com.el.discount.domain.Discount;
import com.el.discount.domain.DiscountRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.money.MonetaryAmount;

@Service
public class DiscountServiceImpl implements DiscountService {

    private final DiscountRepository discountRepository;

    public DiscountServiceImpl(DiscountRepository discountRepository) {
        this.discountRepository = discountRepository;
    }

    @Override
    public Page<Discount> findAll(Pageable pageable) {
        return discountRepository.findAllByDeleted(false, pageable);
    }

    @Override
    public Discount findById(Long id) {
        return discountRepository.findByIdAndDeleted(id, false)
                .orElseThrow(ResourceNotFoundException::new);
    }

    @Override
    public Discount findByCode(String code) {
        Discount discount = discountRepository.findByCodeAndDeleted(code, false)
                .orElseThrow(ResourceNotFoundException::new);
        if (!discount.isActive())
            throw new ResourceNotFoundException();
        return discount;
    }

    @Override
    public Page<Discount> findTrashedDiscount(Pageable pageable) {
        return discountRepository.findAllByDeleted(true, pageable);
    }

    @Override
    public Discount findDeletedDiscountById(Long id) {
        return discountRepository.findByIdAndDeleted(id, true)
                .orElseThrow(ResourceNotFoundException::new);
    }

    @Override
    public void increaseUsage(String code) {
        var discount = findByCode(code);
        discount.increaseUsage();
        discountRepository.save(discount);
    }

    @Override
    public MonetaryAmount calculateDiscount(String code, MonetaryAmount originalPrice) {
        var discount = findByCode(code);
        return discount.calculateDiscount(originalPrice);
    }

    @Override
    public Discount createDiscount(DiscountDTO discountDTO) {
        if (discountRepository.existsByCode(discountDTO.code())) {
            throw new InputInvalidException("Discount code already exists");
        }
        return discountRepository.save(discountDTO.toDiscount());
    }

    @Override
    public Discount updateDiscount(Long id, DiscountDTO discountDTO) {
        var discount = findById(id);
        if (!discountDTO.code().equals(discount.getCode()) &&
                discountRepository.existsByCode(discountDTO.code())) {
            throw new InputInvalidException("Discount code already exists");
        }
        discount.updateInfo(discountDTO.toDiscount());
        return discountRepository.save(discount);
    }

    @Override
    public void deleteDiscount(Long id) {
        var discount = findById(id);
        discount.delete();
        discountRepository.save(discount);
    }

    @Override
    public void restoreDiscount(Long id) {
        var discount = findDeletedDiscountById(id);
        discount.restore();
        discountRepository.save(discount);
    }

    @Override
    public void forceDeleteDiscount(Long id) {
        var discount = findDeletedDiscountById(id);
        discountRepository.delete(discount);
    }

    @Override
    public DiscountSearchDTO searchDiscountByCode(String code, MonetaryAmount originalPrice) {
        var discount = findByCode(code);
        if (discount.isMismatchCurrency(originalPrice)) {
            throw new ResourceNotFoundException();
        }
        MonetaryAmount discountedPrice = discount.calculateDiscount(originalPrice);
        return new DiscountSearchDTO(discount.getCode(),
                discount.getType(),
                discount.getPercentage(),
                discount.getMaxValue(),
                discount.getFixedPrice(),
                discount.getStartDate(),
                discount.getEndDate(),
                discountedPrice);
    }


}
