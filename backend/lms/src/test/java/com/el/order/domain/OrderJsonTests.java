package com.el.order.domain;

import com.el.TestFactory;
import com.el.common.Currencies;
import com.el.common.TimeUtils;
import com.el.common.config.jackson.JacksonCustomizations;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.context.annotation.Import;

import java.io.IOException;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled
@JsonTest
@Import(JacksonCustomizations.class)
class OrderJsonTests {

    @Autowired
    private JacksonTester<Order> json;

    @Test
    void serialize() throws IOException {
        // Arrange
        Set<OrderItem> items = new HashSet<>();
        items.add(new OrderItem(111L, Money.of(1000, Currencies.VND)));
        items.add(new OrderItem(222L, Money.of(2000, Currencies.VND)));
        Order order = new Order(items, TestFactory.userId);

        // Mock
        Order orderMock = Mockito.spy(order);
        Mockito.when(orderMock.getId()).thenReturn(UUID.randomUUID()); // Mocking id
        Mockito.when(orderMock.getCreatedBy()).thenReturn("user101"); // Mocking createdBy
        var createdDate = Instant.now();
        var createdDateString = TimeUtils.FORMATTER.format(createdDate);
        Mockito.when(orderMock.getCreatedDate()).thenReturn(createdDate); // Mocking createdBy

        JsonContent<Order> jsonContent = json.write(orderMock);
        assertThat(jsonContent).extractingJsonPathStringValue("@.id").isNotNull();
        assertThat(jsonContent).extractingJsonPathNumberValue("@.items.length()").isEqualTo(2);
        assertThat(jsonContent).extractingJsonPathStringValue("@.status").isEqualTo("PENDING");
        assertThat(jsonContent).extractingJsonPathStringValue("@.orderDate").isNotNull();
        assertThat(jsonContent).extractingJsonPathStringValue("@.totalPrice").isEqualTo("VND3,000.00");
        assertThat(jsonContent).extractingJsonPathStringValue("@.createdBy").isEqualTo("user101");
        assertThat(jsonContent).extractingJsonPathStringValue("@.createdDate").isEqualTo(createdDateString);
    }

}
