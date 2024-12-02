package com.el.order.application;

import com.el.order.web.dto.OrderRequestDTO;
import com.el.order.domain.Order;
import org.springframework.data.domain.Pageable;

import javax.money.MonetaryAmount;
import java.util.List;
import java.util.UUID;

public interface OrderService {

    List<Order> findOrdersByCreatedBy(String createdBy, Pageable pageable);

    Order findOrderByCreatedByAndId(String createdBy, UUID id);

    Order createOrder(String student, OrderRequestDTO orderRequestDTO);

    Order createOrderExchange(Long courseId, Long enrollmentId, MonetaryAmount additionalPrice);

    void paymentSucceeded(UUID orderId);

}
