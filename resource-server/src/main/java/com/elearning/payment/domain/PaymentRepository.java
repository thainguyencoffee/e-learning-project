package com.elearning.payment.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface PaymentRepository extends CrudRepository<Payment, UUID> {

    Page<Payment> findAllByOrderId(UUID orderId, Pageable pageable);

}
