package com.elearning.order.domain;

import com.elearning.common.Currencies;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class OrderTests {

    @Test
    void orderConstructor_ValidInput_CreatesOrder() {
        // Arrange
        Set<OrderItem> items = Set.of(
                new OrderItem(1L, Money.of(1000, Currencies.VND)),
                new OrderItem(2L, Money.of(2000, Currencies.VND))
        );
        // Act
        Order order = new Order(items);
        // Assert
        // Check if the order has been created with the correct items
        assertEquals(order.getItems().size(), 2);
        // Check if the order has been created with the correct order date
        assertNotNull(order.getOrderDate());
        // Check if the order has been created with the correct status
        assertEquals(order.getStatus(), Status.PENDING);
        // Check if the order has been created with the correct total price
        assertEquals(order.getTotalPrice(), Money.of(3000, Currencies.VND));
        // Check if the order has been created with no discount code
        assertNull(order.getDiscountCode());
        // Check if the order has been created with no discounted price
        assertNull(order.getDiscountedPrice());
    }

    @Test
    void shouldThrowExceptionWhenItemsIsEmpty() {
        // Arrange: Tạo một set trống
        Set<OrderItem> items = new HashSet<>();

        // Act: Gọi constructor với danh sách trống và chờ ngoại lệ
        Executable executable = () -> new Order(items);

        // Assert: Kiểm tra ngoại lệ IllegalArgumentException
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, executable);
        assertEquals("Order must contain at least one item.", exception.getMessage());
    }

}
