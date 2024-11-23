package com.el.discount.application;

import com.el.discount.web.dto.DiscountDTO;
import com.el.discount.application.dto.DiscountSearchDTO;
import com.el.discount.domain.Discount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.money.MonetaryAmount;

public interface DiscountService {

    Page<Discount> findAll(Pageable pageable);

    Discount findById(Long id);

    Discount findByCode(String code);

    Page<Discount> findTrashedDiscount(Pageable pageable);

    Discount findDeletedDiscountById(Long id);

    MonetaryAmount calculateDiscount(String code, MonetaryAmount originalPrice);

    void increaseUsage(String code);

    Discount createDiscount(DiscountDTO discountDTO);

    Discount updateDiscount(Long id, DiscountDTO discountDTO);

    void deleteDiscount(Long id);

    void restoreDiscount(Long id);

    void forceDeleteDiscount(Long id);

    DiscountSearchDTO searchDiscountByCode(String code, MonetaryAmount originalPrice);
}
