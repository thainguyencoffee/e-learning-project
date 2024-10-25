package com.el.order.web;

import com.el.order.application.OrderService;
import com.el.order.application.dto.OrderRequestDTO;
import com.el.order.domain.Order;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/orders", produces = "application/json")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<Page<Order>> orders(Pageable pageable) {
        return ResponseEntity.ok(orderService.findAllOrders(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> orderById(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.findOrderById(id));
    }

    @GetMapping("/my-orders")
    public ResponseEntity<List<Order>> myOrders(@AuthenticationPrincipal Jwt jwt, Pageable pageable) {
        return ResponseEntity.ok(orderService.findOrdersByCreatedBy(jwt.getSubject(), pageable));
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody OrderRequestDTO orderRequestDTO) {
        String student = jwt.getSubject();
        Order orderCreated = orderService.createOrder(student, orderRequestDTO);
        return ResponseEntity.created(URI.create("/orders/" + orderCreated.getId())).body(orderCreated);
    }


}
