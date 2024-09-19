package com.elearning.discount.domain;

import lombok.Data;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Table;

import javax.money.MonetaryAmount;
import java.time.Instant;

@Data
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

    public boolean isValid() {
        return startDate.isBefore(Instant.now()) && endDate.isAfter(Instant.now());
    }

    public MonetaryAmount calculateDiscount(MonetaryAmount source) {
        if (type == Type.PERCENTAGE) {
            return source.multiply(percentage / 100.0);
        } else if (type == Type.FIXED) {
            return fixedAmount;
        }
        throw new IllegalStateException("Unknown discount type");
    }

}
