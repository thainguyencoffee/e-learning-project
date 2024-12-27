package com.el.discount.domain;

import com.el.common.MoneyUtils;
import com.el.common.exception.InputInvalidException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import org.javamoney.moneta.Money;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Table;

import javax.money.CurrencyUnit;
import javax.money.MonetaryAmount;
import java.time.LocalDateTime;

@Getter
@Table("discount")
public class Discount {
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
    @CreatedBy
    private String createdBy;
    @CreatedDate
    private LocalDateTime createdDate;
    @LastModifiedBy
    private String lastModifiedBy;
    @LastModifiedDate
    private LocalDateTime lastModifiedDate;

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
            throw new InputInvalidException("Discount must active and not expired to be used.");
        }

        if (canIncreaseUsage()) {
            if (isMismatchCurrency(originalPrice)) {
                throw new InputInvalidException("Currency mismatch. Discount currency must be the same as the original price.");
            }
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


    public boolean isMismatchCurrency(MonetaryAmount originalPrice) {
        CurrencyUnit currency = this.type == Type.PERCENTAGE ? this.maxValue.getCurrency() : this.fixedPrice.getCurrency();
        return !originalPrice.getCurrency().equals(currency);
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

        if (type == Type.PERCENTAGE) {
            if (percentage == null) {
                throw new InputInvalidException("Discount percentage must be provided.");
            }
            if (percentage < 0 || percentage > 100)
                throw new InputInvalidException("Percentage must be between 0 and 100.");
            if (maxValue == null)
                throw new InputInvalidException("Max value must be provided for percentage discount.");
            MoneyUtils.checkValidPrice(maxValue);
        } else {
            if (fixedPrice == null)
                throw new InputInvalidException("Fixed price must be provided for fixed discount.");
            MoneyUtils.checkValidPrice(fixedPrice);
        }

        validateDates();
    }

    private void validateDates() {
        if (startDate == null || endDate == null) {
            throw new InputInvalidException("Start date and end date must be provided.");
        }

        if (startDate.isAfter(endDate)) {
            throw new InputInvalidException("Start date cannot be after end date.");
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
