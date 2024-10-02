package com.elearning.order.application.impl;

import com.elearning.common.exception.ResourceNotFoundException;
import com.elearning.course.application.CourseService;
import com.elearning.course.domain.Course;
import com.elearning.discount.application.DiscountService;
import com.elearning.order.application.OrderService;
import com.elearning.order.application.dto.OrderItemDTO;
import com.elearning.order.application.dto.OrderRequestDTO;
import com.elearning.order.domain.Order;
import com.elearning.order.domain.OrderItem;
import com.elearning.order.domain.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.money.MonetaryAmount;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final CourseService courseService;
    private final OrderRepository orderRepository;
    private final DiscountService discountService;

    public OrderServiceImpl(CourseService courseService, OrderRepository orderRepository, DiscountService discountService) {
        this.courseService = courseService;
        this.orderRepository = orderRepository;
        this.discountService = discountService;
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
    public Order createOrder(OrderRequestDTO orderRequestDTO) {
        Set<OrderItem> items = new HashSet<>();

        for (OrderItemDTO itemDto : orderRequestDTO.items()) {
            Course course = courseService.findPublishedCourseById(itemDto.id());
            Long courseId = course.getId();
            MonetaryAmount finalPrice = course.getFinalPrice();
            items.add(new OrderItem(courseId, finalPrice));
        }

        Order newOrder = new Order(items);

        if (orderRequestDTO.discountCode() != null) {
            // Apply discount code
            MonetaryAmount monetaryAmount = discountService.calculateDiscount(
                    orderRequestDTO.discountCode(),
                    newOrder.getTotalPrice());
            newOrder.applyDiscount(monetaryAmount, orderRequestDTO.discountCode());
        }

        return orderRepository.save(newOrder);
    }

}