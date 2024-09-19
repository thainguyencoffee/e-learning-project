package com.elearning.course.domain;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.javamoney.moneta.Money;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.util.Assert;

import javax.money.MonetaryAmount;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Data
@Table("course")
@Slf4j
public class Course {
    @Id
    private Long id;
    private String title;
    @Embedded.Nullable
    private Audience audience;
    @MappedCollection(idColumn = "course")
    private Set<CourseSection> sections = new HashSet<>();
    private MonetaryAmount price;
    private MonetaryAmount discountedPrice;
    private String description;
    @CreatedBy
    private String createdBy;
    @CreatedDate
    private Instant createdDate;
    @LastModifiedBy
    private String lastModifiedBy;
    @LastModifiedDate
    private Instant lastModifiedDate;

    private Set<StudentRef> students = new HashSet<>();
    private Long discountId;

    public Course(String title, MonetaryAmount price, String description, Audience audience) {
        Assert.hasText(title, "Title must not be empty");
        Assert.notNull(price, "Price must not be null");
        Assert.hasText(description, "Description must not be empty");
        Assert.notNull(audience, "Audience must not be null");

        this.title = title;
        this.price = price;
        this.discountedPrice = getFinalPrice();
        this.description = description;
        this.audience = audience;
    }

    public void applyDiscount(MonetaryAmount discountAmount) {
        if (discountAmount == null) {
            throw new IllegalArgumentException("Discount amount must not be null");
        }
        if (this.price.subtract(discountAmount).isNegativeOrZero()) {
            this.discountedPrice = Money.zero(this.price.getCurrency());
        } else {
            this.discountedPrice = this.price.subtract(discountAmount);
        }
    }

    private MonetaryAmount getFinalPrice() {
        return (this.discountedPrice != null) ? this.discountedPrice : this.price;
    }

    public void addSection(CourseSection section) {
        if (section == null) {
            throw new IllegalArgumentException("Section can't be null");
        }
        this.sections.add(section);
    }

    public void removeSectionsOrphan(Set<Long> validSectionIds) {
        this.sections.removeIf(section -> section.getId() != null && !validSectionIds.contains(section.getId()));
    }

    public CourseSection findSectionById(Long sectionId) {
        return this.sections.stream()
                .filter(section -> section.getId().equals(sectionId))
                .findFirst()
                .orElse(null);
    }

    public void updateInfo(String title, MonetaryAmount price, String description, Audience audience) {
        Assert.hasText(title, "Title must not be empty");
        Assert.notNull(price, "Price must not be null");
        Assert.hasText(description, "Description must not be empty");
        Assert.notNull(audience, "Audience must not be null");

        this.title = title;
        this.price = price;
        this.discountedPrice = getFinalPrice();
        this.description = description;
        this.audience = audience;
    }
}
