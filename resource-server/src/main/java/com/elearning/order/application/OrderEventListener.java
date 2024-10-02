package com.elearning.order.application;

import com.elearning.order.domain.Order;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventListener {

    @EventListener
    public void handleOrderCreatedEvent(Order.OrderCreatedEvent event) {
        System.out.println("Order created: " + event.orderId());
    }

}
