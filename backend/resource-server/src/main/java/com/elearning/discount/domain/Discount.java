package com.elearning.discount.domain;

import com.elearning.common.AuditSupportClass;
import com.elearning.common.exception.InputInvalidException;
import com.elearning.discount.domain.exception.DiscountInvalidDateException;
import lombok.Getter;
import org.javamoney.moneta.Money;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import javax.money.MonetaryAmount;
import java.time.Instant;

@Getter
@Table("discount")
public class Discount extends AuditSupportClass {
    @Id
    private Long id;
    private String code;
    private Type type;
    private Double percentage;
    private MonetaryAmount fixedAmount;
    private Instant startDate;
    private Instant endDate;

    // Constructor với các logic kiểm tra
    public Discount(
            String code,
            Type type,
            Double percentage,
            MonetaryAmount fixedAmount,
            Instant startDate,
            Instant endDate
    ) {
        this.code = code;
        this.type = type;
        this.percentage = percentage;
        this.fixedAmount = fixedAmount;
        this.startDate = startDate;
        this.endDate = endDate;

        validateDiscount();
    }

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

    private void validateDiscount() {
        if(code == null || code.isBlank()) {
            throw new InputInvalidException("Discount code must not be empty.");
        }
        if (type == null) {
            throw new InputInvalidException("Discount type must not be empty.");
        }
        if (percentage == null && fixedAmount == null) {
            throw new InputInvalidException("Discount percentage or fixed amount must be provided.");
        }
        if (startDate == null || endDate == null) {
            throw new InputInvalidException("Start date and end date must be provided.");
        }

        validateDates();
        validateDiscountType();
    }

    // Kiểm tra ngày hợp lệ
    private void validateDates() {
        if (startDate.isAfter(endDate)) {
            throw new InputInvalidException("Start date cannot be after end date.");
        }
    }

    // Kiểm tra loại giảm giá và giá trị tương ứng
    private void validateDiscountType() {
        if (type == Type.PERCENTAGE && (percentage < 0 || percentage > 100)) {
            throw new InputInvalidException("Percentage must be between 0 and 100.");
        }

        if (type == Type.FIXED && fixedAmount.isLessThan(Money.zero(fixedAmount.getCurrency()))) {
            throw new InputInvalidException("Fixed amount must be greater than zero.");
        }
    }

}
