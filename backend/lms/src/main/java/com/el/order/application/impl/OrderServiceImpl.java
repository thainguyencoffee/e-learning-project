package com.el.order.application.impl;

import com.el.common.RolesBaseUtil;
import com.el.common.exception.AccessDeniedException;
import com.el.common.exception.ResourceNotFoundException;
import com.el.course.application.CourseQueryService;
import com.el.course.domain.Course;
import com.el.discount.application.DiscountService;
import com.el.order.application.OrderService;
import com.el.order.domain.*;
import com.el.order.web.dto.OrderItemDTO;
import com.el.order.web.dto.OrderRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.money.MonetaryAmount;
import java.util.*;

@Slf4j
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

    public void makeCancelledAllOrderByCourseId(UUID orderId) {
        log.info("Cancelling all orders by id: {}", orderId);

        // now only support order contain only one course
        Order order = findOrderById(orderId);
        Long courseId = order.getOrderType() == OrderType.PURCHASE ? order.getACourseIdOnly() : order.getExchangeDetails().courseId();

        List<Order> ordersPending = orderRepository.findAllOrderPendingByCourseId(courseId);
        for (Order orderPending : ordersPending) {
            orderPending.cancelOrder();
            orderRepository.save(orderPending);
        }
    }

    @Override
    public Order createOrder(String currentUsername, OrderRequestDTO orderRequestDTO) {
        if (rolesBaseUtil.isAdmin() || rolesBaseUtil.isTeacher()) {
            throw new AccessDeniedException("Only authenticated users can create orders");
        }
        Set<OrderItem> items = new LinkedHashSet<>();

        for (OrderItemDTO itemDto : orderRequestDTO.items()) {
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

    public Order createOrderExchange(Long courseId, Long enrollmentId, MonetaryAmount additionalPrice) {
        if (rolesBaseUtil.isAdmin() || rolesBaseUtil.isTeacher()) {
            throw new AccessDeniedException("Only authenticated users can create orders");
        }

        Order newOrder = new Order(new ExchangeDetails(enrollmentId, courseId, additionalPrice));
        return orderRepository.save(newOrder);
    }

}
