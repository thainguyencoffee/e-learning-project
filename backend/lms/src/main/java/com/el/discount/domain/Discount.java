package com.el.discount.domain;

import com.el.common.AuditSupportClass;
import com.el.common.Currencies;
import com.el.common.exception.InputInvalidException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import org.javamoney.moneta.Money;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import javax.money.CurrencyUnit;
import javax.money.MonetaryAmount;
import javax.money.NumberValue;
import java.time.LocalDateTime;

@Getter
@Table("discount")
public class Discount extends AuditSupportClass {
    @Id
    private Long id;
    private String code;
    private Type type;
    private Double percentage;
    private MonetaryAmount maxValue;
    private MonetaryAmount fixedPrice;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer currentUsage;
    private Integer maxUsage;
    @JsonIgnore
    private boolean deleted;

    public Discount(
            String code,
            Type type,
            Double percentage,
            MonetaryAmount maxValue,
            MonetaryAmount fixedPrice,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Integer maxUsage
    ) {
        this.code = code;
        this.type = type;

        // Percentage
        this.percentage = percentage;
        this.maxValue = maxValue;

        // Fixed
        this.fixedPrice = fixedPrice;
        this.startDate = startDate;
        this.endDate = endDate;
        this.maxUsage = maxUsage;
        this.deleted = false;
        this.currentUsage = 0;

        validateDiscount();
        validateMoneyNumber();
    }

    private void validateMoneyNumber() {
        if (type == Type.PERCENTAGE) {
            NumberValue number = this.maxValue.getNumber();
            CurrencyUnit currency = this.maxValue.getCurrency();
            if (currency == Currencies.VND) {
                if (number.intValue() % 1000 != 0) {
                    throw new InputInvalidException("Vietnamese currency must be a multiple of 1000.");
                }
            }
        }
        if (type == Type.FIXED) {
            NumberValue number = this.fixedPrice.getNumber();
            CurrencyUnit currency = this.fixedPrice.getCurrency();
            if (currency == Currencies.VND) {
                if (number.intValue() % 1000 != 0) {
                    throw new InputInvalidException("Vietnamese currency must be a multiple of 1000.");
                }
            }
        }
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
                MonetaryAmount multiply = originalPrice.multiply(this.percentage / 100);
                if (multiply.isGreaterThan(this.maxValue)) {
                    return this.maxValue;
                }
                return multiply;
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

    private void validateDates() {
        if (startDate.isAfter(endDate)) {
            throw new InputInvalidException("Start date cannot be after end date.");
        }
    }

    private void validateDiscountType() {
        if (type == Type.PERCENTAGE && (percentage < 0 || percentage > 100)) {
            throw new InputInvalidException("Percentage must be between 0 and 100.");
        }

        if (type == Type.PERCENTAGE && maxValue == null) {
            throw new InputInvalidException("Max value must be provided for percentage discount.");
        }

        if (type == Type.PERCENTAGE && maxValue.isLessThan(Money.zero(maxValue.getCurrency()))) {
            throw new InputInvalidException("Max value must be greater than zero.");
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
        this.maxValue = discount.maxValue;
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
