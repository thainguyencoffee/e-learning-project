package com.el.course.domain;

import com.el.common.AuditSupportClass;
import com.el.common.Currencies;
import com.el.common.exception.ResourceNotFoundException;
import com.el.common.exception.InputInvalidException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import org.apache.commons.lang3.Validate;
import org.javamoney.moneta.Money;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.util.Assert;

import javax.money.CurrencyUnit;
import javax.money.MonetaryAmount;
import java.util.HashSet;
import java.util.Set;

@Getter
@Table("course")
public class Course extends AuditSupportClass {
    @Id
    private Long id;
    private String title;
    private String thumbnailUrl;
    private String description;
    private final Language language;
    private Set<Language> subtitles;
    private Set<String> benefits;
    private Set<String> prerequisites;

    @MappedCollection(idColumn = "course")
    private Set<CourseSection> sections = new HashSet<>();
    private MonetaryAmount price;
    private MonetaryAmount discountedPrice;
    private Boolean published;
    private String teacher;
    private String approvedBy;
    private Set<StudentRef> students = new HashSet<>();
    private String discountCode;
    @JsonIgnore
    private boolean deleted;
    @Version
    private int version;

    public Course(
            String title,
            String description,
            String thumbnailUrl,
            Set<String> benefits,
            Language language,
            Set<String> prerequisites,
            Set<Language> subtitles,
            String teacher
    ) {

        Assert.hasText(title, "Title must not be empty.");

        this.title = title;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.language = language;
        this.benefits = benefits;
        this.prerequisites = prerequisites;
        this.subtitles = subtitles;
        this.teacher = teacher;
        deleted = false;
        published = false;
    }

    public void updateInfo(
            String title,
            String description,
            String thumbnailUrl,
            Set<String> benefits,
            Set<String> prerequisites,
            Set<Language> subtitles
    ) {
        if (!canEdit()) {
            throw new InputInvalidException("Cannot update a published course.");
        }

        Assert.hasText(title, "Title must not be empty.");

        this.title = title;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.benefits = benefits;
        this.prerequisites = prerequisites;
        this.subtitles = subtitles;
    }

    public void changePrice(MonetaryAmount newPrice) {
        if (!canEdit()) {
            throw new InputInvalidException("Cannot change price of a published course.");
        }
        if (!isValidCurrency(newPrice.getCurrency())) {
            throw new InputInvalidException("Currency is not supported. We support VND only.");
        }
        if (newPrice.isLessThan(Money.zero(newPrice.getCurrency()))) {
            throw new InputInvalidException("Price cannot be negative.");
        }
        this.price = newPrice;
    }

    public void applyDiscount(MonetaryAmount discountedPrice, String discountCode) {
        if (this.getPrice() == null) {
            throw new InputInvalidException("Cannot apply discount to a course without a price.");
        }
        Validate.notNull(discountedPrice, "Discounted price must not be null.");

        this.discountCode = discountCode;

        if (this.price.subtract(discountedPrice).isNegativeOrZero()) {
            this.discountedPrice = Money.zero(this.price.getCurrency());
        } else {
            this.discountedPrice = this.price.subtract(discountedPrice);
        }
    }

    public MonetaryAmount getFinalPrice() {
        return this.discountedPrice != null ? this.discountedPrice : this.price;
    }

    public void assignTeacher(String teacher) {
        if (!canEdit()) {
            throw new InputInvalidException("Cannot assign a teacher to a published course.");
        }

        Validate.notNull(teacher, "Teacher must not be null.");

        this.teacher = teacher;
    }

    public void publish(String approvedBy) {
        if (!canEdit()) {
            throw new InputInvalidException("Course is already published.");
        }
        validateForPublish(approvedBy);
        this.approvedBy = approvedBy;
        this.published = true;
    }

    private void validateForPublish(String approvedBy) {
        if (this.getSections().isEmpty() || this.getPrice() == null || this.getTeacher() == null) {
            throw new InputInvalidException("Cannot publish a course without sections, price or teacher.");
        }
        if (approvedBy == null || approvedBy.isEmpty()) {
            throw new InputInvalidException("Approved by must not be empty.");
        }
        if (approvedBy.equals(this.getTeacher())) {
            throw new InputInvalidException("Teacher cannot approve their own course.");
        }
    }

    public void addSection(CourseSection section) {
        if (!canEdit()) {
            throw new InputInvalidException("Cannot add a section to a published course.");
        }

        Validate.notNull(section, "Section must not be null.");

        if (this.sections.stream().anyMatch(existingSection -> existingSection.getTitle().equals(section.getTitle()))) {
            throw new InputInvalidException("A section with the same title already exists.");
        }

//        if (section.getLessons().isEmpty()) {
//            throw new InputInvalidException("Section must have at least one lesson.");
//        }

        this.sections.add(section);
    }

    public void updateSection(Long sectionId, String title) {
        if (!canEdit()) {
            throw new InputInvalidException("Cannot update a section in a published course.");
        }

        CourseSection existingSection = findSectionById(sectionId);

        if (this.sections.stream().anyMatch(section -> section.getTitle().equals(title))) {
            throw new InputInvalidException("A section with the same title already exists.");
        }

        existingSection.updateInfo(title);
    }

    public void removeSection(Long sectionId) {
        if (!canEdit()) {
            throw new InputInvalidException("Cannot remove a section from a published course.");
        }
        CourseSection courseSection = findSectionById(sectionId);
        this.sections.remove(courseSection);
    }

    public void addLessonToSection(Long sectionId, Lesson lesson) {
        if (!canEdit()) {
            throw new InputInvalidException("Cannot add a lesson to a published course.");
        }
        CourseSection section = findSectionById(sectionId);
        section.addLesson(lesson);
    }

    public void updateLessonInSection(Long sectionId, Long lessonId, Lesson updatedLesson){
        if (!canEdit()) {
            throw new InputInvalidException("Cannot add a lesson to a published course.");
        }
        CourseSection section = findSectionById(sectionId);
        section.updateLesson(lessonId, updatedLesson);
    }

    public void removeLessonFromSection(Long sectionId, Long lessonId){
        if (!canEdit()) {
            throw new InputInvalidException("Cannot add a lesson to a published course.");
        }
        CourseSection section = findSectionById(sectionId);
        section.removeLesson(lessonId);
    };

    public void delete() {
        if (!canEdit()) {
            throw new InputInvalidException("Cannot delete a published course.");
        }
        if (this.deleted) {
            throw new InputInvalidException("Course is already deleted.");
        }
        this.deleted = true;
    }

    public void restore() {
        if (!this.deleted) {
            throw new InputInvalidException("Course is not deleted.");
        }
        this.deleted = false;
    }

    private CourseSection findSectionById(Long sectionId) {
        return this.sections.stream()
                .filter(section -> section.getId().equals(sectionId))
                .findFirst()
                .orElseThrow(ResourceNotFoundException::new);
    }

    public boolean canEdit() {
        return !published;
    }

    public boolean isValidCurrency(CurrencyUnit inputCurrency) {
        var validCurrencies = Set.of(Currencies.VND);
        return validCurrencies.contains(inputCurrency);
    }

}
