package com.el.discount.domain;

import com.el.common.AuditSupportClass;
import com.el.common.exception.InputInvalidException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import org.javamoney.moneta.Money;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import javax.money.MonetaryAmount;
import java.time.LocalDateTime;

@Getter
@Table("discount")
public class Discount extends AuditSupportClass {
    @Id
    private Long id;
    private String code;
    private Type type;
    private Double percentage;
    private MonetaryAmount fixedPrice;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer currentUsage;
    private Integer maxUsage;
    @JsonIgnore
    private boolean deleted;

    // Constructor với các logic kiểm tra
    public Discount(
            String code,
            Type type,
            Double percentage,
            MonetaryAmount fixedPrice,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Integer maxUsage
    ) {
        this.code = code;
        this.type = type;
        this.percentage = percentage;
        this.fixedPrice = fixedPrice;
        this.startDate = startDate;
        this.endDate = endDate;
        this.maxUsage = maxUsage;
        this.deleted = false;
        this.currentUsage = 0;

        validateDiscount();
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(endDate);
    }

    public boolean isActive() {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(startDate) && now.isBefore(endDate);
    }

    public boolean isValid() {
        return !isExpired() && isActive();
    }

    public MonetaryAmount calculateDiscount(MonetaryAmount originalPrice) {
        if (!isValid()) {
            throw new InputInvalidException("Discount is not valid.");
        }

        if (canIncreaseUsage()) {
            if (this.type == Type.PERCENTAGE) {
                return originalPrice.multiply(this.percentage / 100);
            } else if (this.type == Type.FIXED) {
                return this.fixedPrice;
            }
        }
        return Money.zero(originalPrice.getCurrency());
    }

    public void increaseUsage() {
        if (canIncreaseUsage()) {
            currentUsage++;
        }
    }

    private boolean canIncreaseUsage() {
        if (currentUsage >= maxUsage) {
            throw new InputInvalidException("Discount has reached its maximum usage.");
        }
        return true;
    }

    private void validateDiscount() {
        if (code == null || code.isBlank()) {
            throw new InputInvalidException("Discount code must not be empty.");
        }
        if (code.contains(" ")) {
            throw new InputInvalidException("Discount code must not contain spaces.");
        }
        if (type == null) {
            throw new InputInvalidException("Discount type must not be empty.");
        }
        if (percentage == null && fixedPrice == null) {
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

        if (type == Type.FIXED && fixedPrice.isLessThan(Money.zero(fixedPrice.getCurrency()))) {
            throw new InputInvalidException("Fixed amount must be greater than zero.");
        }
    }

    public void updateInfo(Discount discount) {

        if (this.currentUsage >= 1) {
            throw new InputInvalidException("Cannot update a discount that has been used.");
        }

        this.code = discount.code;
        this.type = discount.type;
        this.percentage = discount.percentage;
        this.fixedPrice = discount.fixedPrice;
        this.startDate = discount.startDate;
        this.endDate = discount.endDate;
        this.maxUsage = discount.maxUsage;

        validateDiscount();
    }

    public void delete() {
        if (this.currentUsage >= 1) {
            throw new InputInvalidException("Cannot delete a discount that has been used.");
        }
        this.deleted = true;
    }


    public void restore() {
        this.deleted = false;
    }

}
