package com.el.order.application;

import com.el.order.web.dto.OrderRequestDTO;
import com.el.order.domain.Order;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface OrderService {

    List<Order> findOrdersByCreatedBy(String createdBy, Pageable pageable);

    Order findOrderByCreatedByAndId(String createdBy, UUID id);

    Order createOrder(String student, OrderRequestDTO orderRequestDTO);

    void paymentSucceeded(UUID orderId);

    boolean hasPurchase(String createdBy, Long courseId);

    List<Long> purchasedCourses(String createdBy);

}
