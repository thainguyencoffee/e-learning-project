package com.el.order.domain;

import com.el.TestFactory;
import com.el.common.Currencies;
import com.el.common.exception.InputInvalidException;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class OrderTests {

    @Test
    void orderConstructor_ShouldCreateOrder_WhenValidItems() {
        Order order = TestFactory.createDefaultOrder();

        assertEquals(2, order.getItems().size());
        assertNotNull(order.getOrderDate());
        assertEquals(Status.PENDING, order.getStatus());
        assertEquals(Money.of(3000, Currencies.VND), order.getTotalPrice());
        assertNull(order.getDiscountCode());
        assertNull(order.getDiscountedPrice());
        assertEquals(TestFactory.userId, order.getStudent());
    }

    @Test
    void orderConstructor_ShouldThrowException_WhenItemsIsEmpty() {
        Set<OrderItem> items = new HashSet<>();

        Executable executable = () -> new Order(items, TestFactory.userId);

        assertThrows(InputInvalidException.class, executable, "Order must contain at least one item.");
    }

    @Test
    void makePaid_ShouldMarkOrderAsPaid_WhenOrderIsPending() {
        Set<OrderItem> items = Set.of(new OrderItem(1L, Money.of(1000, Currencies.VND)));
        Order order = new Order(items, TestFactory.userId);

        order.makePaid();

        assertEquals(Status.PAID, order.getStatus());
    }

    @Test
    void makePaid_ShouldThrowException_WhenOrderIsAlreadyPaid() {
        Set<OrderItem> items = Set.of(new OrderItem(1L, Money.of(1000, Currencies.VND)));
        Order order = new Order(items, TestFactory.userId);
        order.makePaid();

        Executable executable = order::makePaid;
        InputInvalidException exception = assertThrows(InputInvalidException.class, executable);
        assertEquals("You can't pay for a completed order.", exception.getMessage());
    }

}
