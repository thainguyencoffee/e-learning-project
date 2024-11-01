package com.el.enrollment.application;

import com.el.common.exception.InputInvalidException;
import com.el.order.domain.Order;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
public class OrderPaidListener {

    private final CourseEnrollmentService courseEnrollmentService;

    public OrderPaidListener(CourseEnrollmentService courseEnrollmentService) {
        this.courseEnrollmentService = courseEnrollmentService;
    }

    @ApplicationModuleListener
    public void handleOrderPaid(Order.OrderPaidEvent e) {
        if (e.items().isEmpty()) throw new InputInvalidException("Order items of OrderPaidEvent has error.");
        for (Long course : e.items()) {
            courseEnrollmentService.enrollment(e.createdBy(), course);
        }
    }

}
