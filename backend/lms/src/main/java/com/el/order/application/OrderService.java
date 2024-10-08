package com.el.order.application;

import com.el.order.application.dto.OrderRequestDTO;
import com.el.order.domain.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface OrderService {

    Page<Order> findAllOrders(Pageable pageable);

    Order findOrderById(UUID orderId);

    List<Order> findOrdersByCreatedBy(String createdBy, Pageable pageable);

    Order createOrder(OrderRequestDTO orderRequestDTO);

}
