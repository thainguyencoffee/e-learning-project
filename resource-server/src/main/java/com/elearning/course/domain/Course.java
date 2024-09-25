package com.elearning.course.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    private String thumbnailUrl;
    @Embedded.Nullable
    private Audience audience;
    @MappedCollection(idColumn = "course")
    private Set<CourseSection> sections = new HashSet<>();
    private MonetaryAmount price;
    private MonetaryAmount discountedPrice;
    private Status status;
    private String description;
    private Term term;
    @CreatedBy
    private String createdBy;
    @CreatedDate
    private Instant createdDate;
    @LastModifiedBy
    private String lastModifiedBy;
    @LastModifiedDate
    private Instant lastModifiedDate;
    private String teacherId;
    private String approvedBy;
    private Language language;
    private Set<Language> subtitles = new HashSet<>();
    private Set<String> benefits = new HashSet<>();
    private Set<String> prerequisites = new HashSet<>();

    private Set<StudentRef> students = new HashSet<>();
    private Long discountId;

    public Course(String title,
                  MonetaryAmount price,
                  String description,
                  Audience audience,
                  String thumbnailUrl,
                  String teacherId,
                  Term term,
                  Language language,
                  Set<Language> subtitles,
                  Set<String> benefits,
                  Set<String> prerequisites) {
        Assert.hasText(title, "Title must not be empty");
        Assert.notNull(price, "Price must not be null");
        Assert.hasText(description, "Description must not be empty");
        Assert.notNull(audience, "Audience must not be null");
        Assert.notNull(thumbnailUrl, "ThumbnailUrl must not be null");
        Assert.notNull(teacherId, "TeacherId must not be null");
        Assert.notNull(term, "Term must not be null");
        Assert.notNull(language, "Language must not be null");

        this.title = title;
        this.price = price;
        this.discountedPrice = getFinalPrice(); // default discounted price is the same as the original price
        this.description = description;
        this.audience = audience;
        this.thumbnailUrl = thumbnailUrl;
        this.status = Status.DRAFT;
        this.teacherId = teacherId;
        this.term = term;
        this.language = language;
        this.subtitles = subtitles;
        this.benefits = benefits;
        this.prerequisites = prerequisites;
    }

    // not business logic :)
    public Course() {
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

    @JsonIgnore
    public MonetaryAmount getFinalPrice() {
        if (this.discountId != null) {
            return (this.discountedPrice != null) ? this.discountedPrice : this.price;
        }
        return this.price;
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

    public void updateInfo(Course courseUpdate) {
        Assert.hasText(title, "Title must not be empty");
        Assert.notNull(price, "Price must not be null");
        Assert.hasText(description, "Description must not be empty");
        Assert.notNull(audience, "Audience must not be null");
        Assert.notNull(thumbnailUrl, "ThumbnailUrl must not be null");
        Assert.notNull(teacherId, "TeacherId must not be null");
        Assert.notNull(term, "Term must not be null");
        Assert.notNull(language, "Language must not be null");

        this.title = courseUpdate.title;
        this.price = courseUpdate.price;
        this.discountedPrice = getFinalPrice();
        this.description = courseUpdate.description;
        this.audience = courseUpdate.audience;
        this.thumbnailUrl = courseUpdate.thumbnailUrl;
        this.teacherId = courseUpdate.teacherId;
        this.term = courseUpdate.term;
        this.language = courseUpdate.language;
        this.subtitles = courseUpdate.subtitles;
        this.benefits = courseUpdate.benefits;
        this.prerequisites = courseUpdate.prerequisites;
    }

}
