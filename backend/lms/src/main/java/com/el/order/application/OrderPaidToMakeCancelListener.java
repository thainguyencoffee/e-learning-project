package com.el.order.application;

import com.el.order.domain.Order;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
public class OrderPaidToMakeCancelListener {

    private final OrderService orderService;

    public OrderPaidToMakeCancelListener(OrderService orderService) {
        this.orderService = orderService;
    }

    @ApplicationModuleListener
    public void makeCancelAllOrderPendingForOrderPurchasePaid(Order.OrderPaidEvent e) {
        orderService.makeCancelledAllOrderByCourseId(e.id());
    }

    @ApplicationModuleListener
    public void makeCancelAllOrderPendingForOrderExchangePaid(Order.OrderExchangePaidEvent e) {
        orderService.makeCancelledAllOrderByCourseId(e.id());
    }

}
