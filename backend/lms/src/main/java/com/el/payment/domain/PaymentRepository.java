package com.el.payment.domain;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface PaymentRepository extends CrudRepository<Payment, UUID> {

    List<Payment> findAllByOrderId(UUID orderId);
}
