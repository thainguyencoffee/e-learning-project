package com.el.order.domain;

import com.el.common.Currencies;
import com.el.common.exception.InputInvalidException;
import lombok.Getter;
import org.javamoney.moneta.Money;
import org.springframework.data.annotation.*;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import javax.money.MonetaryAmount;
import java.time.Instant;
import java.util.*;

@Getter
@Table("orders")
public class Order extends AbstractAggregateRoot<Order> {
    @Id
    private UUID id;
    @MappedCollection(idColumn = "orders")
    private Set<OrderItem> items = new HashSet<>();
    private Instant orderDate;
    private MonetaryAmount totalPrice;
    private MonetaryAmount discountedPrice;
    private String discountCode;
    private Status status;
    @CreatedBy
    private String createdBy;
    @CreatedDate
    private Instant createdDate;
    @LastModifiedBy
    private String lastModifiedBy;
    @LastModifiedDate
    private Instant lastModifiedDate;

    public Order(Set<OrderItem> items) {
        if (items.isEmpty()) throw new InputInvalidException("Order must contain at least one item.");

        this.orderDate = Instant.now();
        this.status = Status.PENDING;

        items.forEach(this::addItem);
    }

    public void addItem(OrderItem item) {
        // Business rule: You can't add items to a completed order
        if (status == Status.PAID || status == Status.CANCELLED) {
            throw new InputInvalidException("You can't add items to a completed order.");
        }
        if (item == null) {
            throw new InputInvalidException("Item can't be null.");
        }

        // Business rule: You can't add the same item to the order
        if (items.contains(item)) {
            throw new InputInvalidException("You can't add the same item to the order.");
        }

        items.add(item);
        calculateTotalPrice();
    }

    public void removeItem(OrderItem item) {
        // Business rule: You can't remove items from a completed order
        if (status == Status.PAID || status == Status.CANCELLED) {
            throw new InputInvalidException("You can't remove items from a completed order.");
        }

        if (item == null) {
            throw new InputInvalidException("Item can't be null.");
        }

        // Business rule: You can't remove an item that doesn't exist in the order
        if (!items.contains(item)) {
            throw new InputInvalidException("You can't remove an item that doesn't exist in the order.");
        }

        items.remove(item);
        calculateTotalPrice();
    }

    public void applyDiscount(MonetaryAmount discountAmount, String discountCode) {
        // Business rule: You can't apply a discount to a completed order
        if (status == Status.PAID || status == Status.CANCELLED) {
            throw new InputInvalidException("You can't apply a discount to a completed order.");
        }

        this.discountCode = discountCode;

        if (this.totalPrice.subtract(discountAmount).isNegativeOrZero()) {
            this.discountedPrice = Money.zero(this.totalPrice.getCurrency());
        } else {
            this.discountedPrice = this.totalPrice.subtract(discountAmount);
        }
    }

    private void calculateTotalPrice() {
        if (items.isEmpty()) {
            this.totalPrice = Money.zero(Currencies.VND);
        } else {
            this.totalPrice = items.stream()
                    .map(OrderItem::getPrice)
                    .reduce(MonetaryAmount::add)
                    .orElse(Money.zero(Currencies.VND));
        }
    }

    public void makePaid() {
        // Business rule: You can't pay for a completed order
        if (isPaid()) {
            throw new InputInvalidException("You can't pay for a completed order.");
        }
        this.status = Status.PAID;
        registerEvent(new OrderPaidEvent(id));
    }

    private boolean isPaid() {
        return status == Status.PAID;
    }

    public void cancelOrder() {
        if (status == Status.PAID) {
            throw new InputInvalidException("You can't cancel a paid order.");
        }
        this.status = Status.CANCELLED;
        registerEvent(new OrderCancelledEvent(id));
    }

    public record OrderPaidEvent(UUID orderId) {}
    public record OrderCancelledEvent(UUID orderId) {}

}
