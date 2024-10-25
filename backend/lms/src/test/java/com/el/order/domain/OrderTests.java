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
}
