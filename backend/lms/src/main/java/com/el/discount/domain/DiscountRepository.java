package com.el.discount.domain;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface DiscountRepository extends CrudRepository<Discount, Long> {

    Optional<Discount> findByCode(String code);

}
