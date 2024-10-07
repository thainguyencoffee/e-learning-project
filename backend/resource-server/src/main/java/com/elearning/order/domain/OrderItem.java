package com.elearning.order.domain;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import javax.money.MonetaryAmount;

@Getter
@Table("order_items")
public class OrderItem {
    @Id
    private Long id;
    private Long course;
    private MonetaryAmount price;

    public OrderItem(Long course, MonetaryAmount price) {
        this.course = course;
        this.price = price;
    }
}
