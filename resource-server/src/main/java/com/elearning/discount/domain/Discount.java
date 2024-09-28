package com.elearning.discount.domain;

import com.elearning.discount.domain.exception.DiscountInvalidDateException;
import lombok.Getter;
import org.javamoney.moneta.Money;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Table;

import javax.money.MonetaryAmount;
import java.time.Instant;

@Getter
@Table("discount")
public class Discount {
    @Id
    private Long id;
    private String code;
    private Type type;
    private double percentage;
    private MonetaryAmount fixedAmount;
    private Instant startDate;
    private Instant endDate;

    @CreatedDate
    private Instant createdDate;
    @CreatedBy
    private String createdBy;
    @LastModifiedDate
    private Instant lastModifiedDate;
    @LastModifiedBy
    private String lastModifiedBy;

    public boolean isExpired() {
        return Instant.now().isAfter(endDate);
    }

    public boolean isActive() {
        Instant now = Instant.now();
        return now.isAfter(startDate) && now.isBefore(endDate);
    }

    public boolean isValid() {
        return !isExpired() && isActive();
    }

    public MonetaryAmount calculateDiscount(MonetaryAmount originalPrice) {
        if (!isValid()) {
            throw new DiscountInvalidDateException("Discount is not valid.");
        }

        if (this.type == Type.PERCENTAGE) {
            return originalPrice.multiply(this.percentage / 100);
        } else if (this.type == Type.FIXED) {
            return this.fixedAmount;
        }
        return Money.zero(originalPrice.getCurrency());
    }
}
