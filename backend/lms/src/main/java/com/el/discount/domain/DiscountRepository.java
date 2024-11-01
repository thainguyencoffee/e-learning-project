package com.el.discount.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface DiscountRepository extends CrudRepository<Discount, Long> {

    Optional<Discount> findByIdAndDeleted(Long id, boolean deleted);

    Page<Discount> findAllByDeleted(boolean deleted, Pageable pageable);

    Optional<Discount> findByCodeAndDeleted(String code, boolean deleted);

    boolean existsByCode(String code);
}
