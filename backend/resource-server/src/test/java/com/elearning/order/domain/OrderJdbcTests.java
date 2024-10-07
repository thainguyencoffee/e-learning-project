package com.elearning.order.domain;

import com.elearning.common.config.DataAuditConfig;
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
        // Arrange: Tạo Order với các item
        OrderItem item = new OrderItem(1L, Money.of(100, "USD"));
        Set<OrderItem> items = new HashSet<>();
        items.add(item);
        order = new Order(items); // Tạo đối tượng Order với 1 item
    }

    @AfterEach
    void tearDown() {
        // Xóa dữ liệu sau mỗi test
        orderRepository.deleteAll();
    }

    @Test
    void testCreateOrder() {
        // Act: Lưu Order vào repository
        Order savedOrder = orderRepository.save(order);

        // Assert: Kiểm tra Order được lưu vào cơ sở dữ liệu
        assertNotNull(savedOrder.getId(), "Order ID should not be null after saving");
        assertEquals(1, savedOrder.getItems().size(), "Order should contain 1 item");
        assertEquals(Status.PENDING, savedOrder.getStatus(), "Order status should be PENDING");
    }

    @Test
    void testFindAllOrdersPaged() {
        orderRepository.save(order);

        Pageable pageable = PageRequest.of(0, 1); // Trang 0, kích thước trang là 2

        // Act: Lấy tất cả các Order với phân trang
        Page<Order> orderPage = orderRepository.findAll(pageable);

        // Assert: Kiểm tra phân trang trả về kết quả đúng
        assertNotNull(orderPage, "Order page should not be null");
        assertEquals(1, orderPage.getSize(), "Page size should be 1");
        assertEquals(1, orderPage.getTotalElements(), "There should be 1 total orders");
        assertEquals(0, orderPage.getNumber(), "The page number should be 0");
        assertEquals(1, orderPage.getTotalPages(), "Total pages should be 1");
    }

    @Test
    void testFindAllByCreatedBy() {
        orderRepository.save(order);

        // Act: Lấy tất cả các Order được tạo bởi 'user1' với phân trang
        List<Order> guestOrders = orderRepository.findAllByCreatedBy("guest", 0, 10); // Trang 0, kích thước 10

        // Assert: Kiểm tra chỉ có các order của 'user1' được trả về
        assertNotNull(guestOrders, "Order list should not be null");
        assertEquals(1, guestOrders.size(), "There should be 1 order for 'guest'");
        assertEquals("guest", guestOrders.get(0).getCreatedBy(), "CreatedBy should be 'guest'");
    }

}
