package com.el.order.application.impl;

import com.el.common.RolesBaseUtil;
import com.el.common.exception.InputInvalidException;
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
    private final RolesBaseUtil rolesBaseUtil;
    private final DiscountService discountService;
    private final CourseQueryService courseQueryService;

    public OrderServiceImpl(OrderRepository orderRepository, RolesBaseUtil rolesBaseUtil, DiscountService discountService, CourseQueryService courseQueryService) {
        this.orderRepository = orderRepository;
        this.rolesBaseUtil = rolesBaseUtil;
        this.discountService = discountService;
        this.courseQueryService = courseQueryService;
    }

    public Order findOrderById(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(ResourceNotFoundException::new);
    }

    @Override
    public List<Order> findOrdersByCreatedBy(String createdBy, Pageable pageable) {
        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();
        if (rolesBaseUtil.isAdmin()) {
            return orderRepository.findAll(pageable).getContent();
        } else {
            return orderRepository.findAllByCreatedBy(createdBy, page, size);
        }
    }

    @Override
    public Order findOrderByCreatedByAndId(String createdBy, UUID id) {
        if (rolesBaseUtil.isAdmin()) {
            return orderRepository.findById(id)
                    .orElseThrow(ResourceNotFoundException::new);
        } else {
            return orderRepository.findByCreatedByAndId(createdBy, id)
                    .orElseThrow(ResourceNotFoundException::new);
        }
    }

    @Override
    public void paymentSucceeded(UUID orderId) {
        Order order = findOrderById(orderId);
        order.makePaid();
        if (order.getDiscountCode() != null && !order.getDiscountCode().isBlank()) {
            discountService.increaseUsage(order.getDiscountCode());
        }
        orderRepository.save(order);
    }

    @Override
    public boolean hasPurchase(String createdBy, Long courseId) {
        return orderRepository.hasPurchasedCourse(courseId, createdBy);
    }

    @Override
    public List<Long> purchasedCourses(String createdBy) {
        return orderRepository.findPurchasedCourseIdsByUserId(createdBy);
    }

    @Override
    public Order createOrder(String currentUsername, OrderRequestDTO orderRequestDTO) {

        Set<OrderItem> items = new HashSet<>();

        for (OrderItemDTO itemDto : orderRequestDTO.items()) {
            if (orderRepository.hasPurchasedCourse(itemDto.id(), currentUsername)) {
                throw new InputInvalidException("You cannot purchase the same course twice");
            }
            Course course = courseQueryService.findPublishedCourseById(itemDto.id());
            items.add(new OrderItem(course.getId(), course.getPrice()));
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
