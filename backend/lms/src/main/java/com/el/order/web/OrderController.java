package com.el.order.web;

import com.el.order.application.OrderService;
import com.el.order.application.dto.OrderRequestDTO;
import com.el.order.domain.Order;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
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

    // Admin can access all orders
    @GetMapping
    public ResponseEntity<Page<Order>> orders(Pageable pageable) {
        return ResponseEntity.ok(orderService.findAllOrders(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> orderById(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.findOrderById(id));
    }

    // User can access only their own orders
    @GetMapping("/my-orders")
    public ResponseEntity<Page<Order>> myOrders(@AuthenticationPrincipal Jwt jwt, Pageable pageable) {
        String createdBy = jwt.getClaim(StandardClaimNames.PREFERRED_USERNAME);
        List<Order> result = orderService.findOrdersByCreatedBy(createdBy, pageable);
        return ResponseEntity.ok(new PageImpl<>(result, pageable, result.size()));
    }

    @GetMapping("/my-orders/{id}")
    public ResponseEntity<Order> myOrderById(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        String createdBy = jwt.getClaim(StandardClaimNames.PREFERRED_USERNAME);
        return ResponseEntity.ok(orderService.findOrderByCreatedByAndId(createdBy, id));
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody OrderRequestDTO orderRequestDTO) {
        String currentUsername = jwt.getClaim(StandardClaimNames.PREFERRED_USERNAME);
        Order orderCreated = orderService.createOrder(currentUsername, orderRequestDTO);
        return ResponseEntity.created(URI.create("/orders/" + orderCreated.getId())).body(orderCreated);
    }

}
