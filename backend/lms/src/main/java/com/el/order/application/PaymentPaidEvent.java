package com.el.order.application;

import com.el.payment.domain.Payment;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentPaidEvent {

    private final OrderService orderService;

    public PaymentPaidEvent(OrderService orderService) {
        this.orderService = orderService;
    }

    @ApplicationModuleListener
    public void handlePaymentPaidEvent(Payment.PaymentPaid e) {
        orderService.paymentSucceeded(e.orderId());
    }

}
