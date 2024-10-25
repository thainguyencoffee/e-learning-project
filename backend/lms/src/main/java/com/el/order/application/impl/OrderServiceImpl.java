package com.el.order.application.impl;

import com.el.common.exception.ResourceNotFoundException;
import com.el.course.application.CourseQueryService;
import com.el.course.domain.Course;
import com.el.discount.application.DiscountService;
import com.el.order.application.OrderService;
import com.el.order.application.dto.OrderItemDTO;
import com.el.order.application.dto.OrderRequestDTO;
import com.el.order.domain.Order;
import com.el.order.domain.OrderItem;
import com.el.order.domain.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.money.MonetaryAmount;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final DiscountService discountService;
    private final CourseQueryService courseQueryService;

    public OrderServiceImpl(OrderRepository orderRepository, DiscountService discountService, CourseQueryService courseQueryService) {
        this.orderRepository = orderRepository;
        this.discountService = discountService;
        this.courseQueryService = courseQueryService;
    }

    @Override
    public Page<Order> findAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    @Override
    public Order findOrderById(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(ResourceNotFoundException::new);
    }

    @Override
    public List<Order> findOrdersByCreatedBy(String createdBy, Pageable pageable) {
        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();
        return orderRepository.findAllByCreatedBy(createdBy, page, size);
    }

    @Override
    public Order createOrder(String student, OrderRequestDTO orderRequestDTO) {
        Set<OrderItem> items = new HashSet<>();

        for (OrderItemDTO itemDto : orderRequestDTO.items()) {
            Course course = courseQueryService.findPublishedCourseById(itemDto.id());
            items.add(new OrderItem(course.getId(), course.getPrice()));
        }

        Order newOrder = new Order(items, student);

        if (orderRequestDTO.discountCode() != null) {
            // Apply discount code
            MonetaryAmount monetaryAmount = discountService.calculateDiscount(
                    orderRequestDTO.discountCode(),
                    newOrder.getTotalPrice());
            newOrder.applyDiscount(monetaryAmount, orderRequestDTO.discountCode());
            discountService.increaseUsage(orderRequestDTO.discountCode());
        }

        return orderRepository.save(newOrder);
    }

}
