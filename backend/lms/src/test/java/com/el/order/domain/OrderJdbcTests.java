package com.el.order.domain;

import com.el.TestFactory;
import com.el.common.Currencies;
import com.el.common.config.DataAuditConfig;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@Import({DataAuditConfig.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("integration")
@DataJdbcTest
class OrderJdbcTests {

    @Autowired
    OrderRepository orderRepository;

    Order order;

    @BeforeEach
    void setUp() {
        // Arrange
        OrderItem item = new OrderItem(1L, Money.of(100, Currencies.VND));
        Set<OrderItem> items = new LinkedHashSet<>();
        items.add(item);
        order = new Order(items);
    }

    @AfterEach
    void tearDown() {
        orderRepository.deleteAll();
    }

    @Test
    void testCreateOrder() {
        // Act
        Order savedOrder = orderRepository.save(order);

        // Assert
        assertNotNull(savedOrder.getId(), "Order ID should not be null after saving");
        assertEquals(1, savedOrder.getItems().size(), "Order should contain 1 item");
        assertEquals(Status.PENDING, savedOrder.getStatus(), "Order status should be PENDING");
    }

    @Test
    void testFindAllOrdersPaged() {
        orderRepository.save(order);

        Pageable pageable = PageRequest.of(0, 1); // Trang 0, kích thước trang là 2

        // Act
        Page<Order> orderPage = orderRepository.findAll(pageable);

        // Assert
        assertNotNull(orderPage, "Order page should not be null");
        assertEquals(1, orderPage.getSize(), "Page size should be 1");
        assertEquals(1, orderPage.getTotalElements(), "There should be 1 total orders");
        assertEquals(0, orderPage.getNumber(), "The page number should be 0");
        assertEquals(1, orderPage.getTotalPages(), "Total pages should be 1");
    }

    @Test
    void testFindAllByCreatedBy() {
        orderRepository.save(order);

        // Act
        List<Order> guestOrders = orderRepository.findAllByCreatedBy("guest", 0, 10);

        // Assert
        assertNotNull(guestOrders, "Order list should not be null");
        assertEquals(1, guestOrders.size(), "There should be 1 order for 'guest'");
        assertEquals("guest", guestOrders.get(0).getCreatedBy(), "CreatedBy should be 'guest'");
    }

    @Test
    void testHasPurchasedCourse() {
        orderRepository.save(order);

        // Act
        boolean hasPurchased = orderRepository.hasPurchasedCourse(1L, "guest");

        // Assert
        assertTrue(hasPurchased, "User should have purchased course 1");
    }

}
