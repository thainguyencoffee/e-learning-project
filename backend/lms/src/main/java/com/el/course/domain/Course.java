package com.el.course.domain;

import com.el.common.AuditSupportClass;
import com.el.common.Currencies;
import com.el.common.exception.ResourceNotFoundException;
import com.el.common.exception.InputInvalidException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import org.javamoney.moneta.Money;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

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
    private Boolean published;
    private Boolean unpublished;
    private String teacher;
    private String approvedBy;
    private Set<StudentRef> students = new HashSet<>();
    private Set<CourseRequest> courseRequests = new HashSet<>();
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

        if (title == null || title.isEmpty()) {
            throw new InputInvalidException("Title must not be empty.");
        }

        if (language == null) {
            throw new InputInvalidException("Language must not be null.");
        }

        if (subtitles != null && subtitles.contains(language)) {
            throw new InputInvalidException("Subtitles must not contain the same language as the course.");
        }

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
        unpublished = false;
    }

    public void updateInfo(
            String title,
            String description,
            String thumbnailUrl,
            Set<String> benefits,
            Set<String> prerequisites,
            Set<Language> subtitles
    ) {
        if (!isNotPublishedAndDeleted()) {
            throw new InputInvalidException("Cannot update a published course.");
        }

        if (title == null || title.isEmpty()) {
            throw new InputInvalidException("Title must not be empty.");
        }

        if (subtitles != null && subtitles.contains(this.language)) {
            throw new InputInvalidException("Subtitles must not contain the same language as the course.");
        }

        this.title = title;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
        this.benefits = benefits;
        this.prerequisites = prerequisites;
        this.subtitles = subtitles;
    }

    public void changePrice(MonetaryAmount newPrice) {
        if (!isNotPublishedAndDeleted()) {
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


    public void assignTeacher(String teacher) {
        if (!isNotPublishedAndDeleted()) {
            throw new InputInvalidException("Cannot assign a teacher to a published course.");
        }

        if (teacher == null || teacher.isEmpty()) {
            throw new InputInvalidException("Teacher must not be empty.");
        }

        this.teacher = teacher;
    }

    public void requestPublish(CourseRequest courseRequest) {
        // if this course is already published, throw exception
        if (isPublishedAndNotDeleted()) {
            throw new InputInvalidException("Cannot request publish for a published course.");
        }
        if (this.getSections().isEmpty() || /*this.getPrice() == null ||*/ this.getTeacher() == null) {
            throw new InputInvalidException("Cannot publish a course without sections or teacher.");
        }
        if (courseRequest.getType() != RequestType.PUBLISH) {
            throw new InputInvalidException("Request type invalid.");
        }
        if (isAnyRequestsUnresolved()) {
            throw new InputInvalidException("Cannot request publish while there are unresolved requests.");
        }
        if (!courseRequest.getRequestedBy().equals(teacher)) {
            throw new InputInvalidException("Only the teacher can request publish.");
        }

        courseRequests.add(courseRequest);
    }

    public void approvePublish(Long courseRequestId, String approvedBy, String approveMessage) {
        if (approveMessage.isBlank()) {
            throw new InputInvalidException("Approve message must not be blank.");
        }
        if (isPublishedAndNotDeleted()) {
            throw new InputInvalidException("Cannot approve publish for a published course.");
        }
        if (isTeacherRequestingSelfApproval(approvedBy)) {
            throw new InputInvalidException("Teacher cannot approve their own course.");
        }
        courseRequests.stream().filter(courseRequest -> courseRequest.getId().equals(courseRequestId)).findFirst()
                .map(unresolvedRequest -> {
                    unresolvedRequest.approve(approvedBy, approveMessage);
                    return unresolvedRequest;
                }).orElseThrow(ResourceNotFoundException::new);
        this.approvedBy = approvedBy;
        this.published = true;
    }

    public void rejectPublish(Long courseRequestId, String rejectedBy, String rejectReason) {
        if (rejectReason.isBlank()) {
            throw new InputInvalidException("Reject reason must not be blank.");
        }
        if (isPublishedAndNotDeleted()) {
            throw new InputInvalidException("Cannot reject publish for a published course.");
        }
        if (isTeacherRequestingSelfApproval(rejectedBy)) {
            throw new InputInvalidException("Teacher cannot reject their own course.");
        }
        courseRequests.stream().filter(course -> course.getId().equals(courseRequestId)).findFirst()
                .map(unresolvedRequest -> {
                    unresolvedRequest.reject(rejectedBy, rejectReason);
                    return unresolvedRequest;
                }).orElseThrow(ResourceNotFoundException::new);
    }

    public void requestUnpublish(CourseRequest courseRequest) {
        if (isNotPublishedAndDeleted()) {
            throw new InputInvalidException("Cannot request unpublish for an unpublished course.");
        }
        if (courseRequest.getType() != RequestType.UNPUBLISH) {
            throw new InputInvalidException("Request type invalid.");
        }
        if (isAnyRequestsUnresolved()) {
            throw new InputInvalidException("Cannot request unpublish while there are unresolved requests.");
        }
        if (!courseRequest.getRequestedBy().equals(teacher)) {
            throw new InputInvalidException("Only the teacher can request unpublish.");
        }

        courseRequests.add(courseRequest);
    }

    public void approveUnpublish(Long courseRequestId, String approvedBy, String approveMessage) {
        if (approveMessage.isBlank()) {
            throw new InputInvalidException("Approve message must not be blank.");
        }
        if (isNotPublishedAndDeleted()) {
            throw new InputInvalidException("Cannot approve unpublish for a published course.");
        }
        if (isTeacherRequestingSelfApproval(approvedBy)) {
            throw new InputInvalidException("Teacher cannot approve unpublish their own course.");
        }
        if (!approvedBy.equals(this.approvedBy)) {
            throw new InputInvalidException("Only the approver can approve unpublish.");
        }
        courseRequests.stream().filter(course -> course.getId().equals(courseRequestId)).findFirst()
                .map(unresolvedRequest -> {
                    unresolvedRequest.approve(approvedBy, approveMessage);
                    return unresolvedRequest;
                }).orElseThrow(ResourceNotFoundException::new);
        this.approvedBy = approvedBy;
        this.published = false;
        this.unpublished = true;
    }

    public void rejectUnpublish(Long courseRequestId, String rejectedBy, String rejectReason) {
        if (rejectReason.isBlank()) {
            throw new InputInvalidException("Reject reason must not be blank.");
        }
        if (isNotPublishedAndDeleted()) {
            throw new InputInvalidException("Cannot reject unpublish for an unpublished course.");
        }
        if (isTeacherRequestingSelfApproval(rejectedBy)) {
            throw new InputInvalidException("Teacher cannot reject unpublish their own course.");
        }
        if (!rejectedBy.equals(this.approvedBy)) {
            throw new InputInvalidException("Only the approver can reject unpublish.");
        }
        courseRequests.stream().filter(course -> course.getId().equals(courseRequestId)).findFirst()
                .map(unresolvedRequest -> {
                    unresolvedRequest.reject(rejectedBy, rejectReason);
                    return unresolvedRequest;
                }).orElseThrow(ResourceNotFoundException::new);
    }

    private boolean isAnyRequestsUnresolved() {
        return courseRequests.stream().anyMatch(CourseRequest::isUnresolved);
    }

    private boolean isTeacherRequestingSelfApproval(String approvedBy) {
        return approvedBy.equals(this.getTeacher());
    }


    public void addSection(CourseSection section) {
        if (!isNotPublishedAndDeleted()) {
            throw new InputInvalidException("Cannot add a section to a published course.");
        }

        if (section == null) {
            throw new InputInvalidException("Section must not be null.");
        }

        if (this.sections.stream().anyMatch(existingSection -> existingSection.getTitle().equals(section.getTitle()))) {
            throw new InputInvalidException("A section with the same title already exists.");
        }

        this.sections.add(section);
    }

    public void updateSection(Long sectionId, String title) {
        if (!isNotPublishedAndDeleted()) {
            throw new InputInvalidException("Cannot update a section in a published course.");
        }

        CourseSection existingSection = findSectionById(sectionId);

        if (this.sections.stream().anyMatch(section -> section.getTitle().equals(title))) {
            throw new InputInvalidException("A section with the same title already exists.");
        }

        existingSection.updateInfo(title);
    }

    public void removeSection(Long sectionId) {
        if (!isNotPublishedAndDeleted()) {
            throw new InputInvalidException("Cannot remove a section from a published course.");
        }
        CourseSection courseSection = findSectionById(sectionId);
        this.sections.remove(courseSection);
    }

    public void addLessonToSection(Long sectionId, Lesson lesson) {
        if (!isNotPublishedAndDeleted()) {
            throw new InputInvalidException("Cannot add a lesson to a published course.");
        }
        CourseSection section = findSectionById(sectionId);
        section.addLesson(lesson);
    }

    public void updateLessonInSection(Long sectionId, Long lessonId, Lesson updatedLesson){
        if (!isNotPublishedAndDeleted()) {
            throw new InputInvalidException("Cannot add a lesson to a published course.");
        }
        CourseSection section = findSectionById(sectionId);
        section.updateLesson(lessonId, updatedLesson);
    }

    public void removeLessonFromSection(Long sectionId, Long lessonId){
        if (!isNotPublishedAndDeleted()) {
            throw new InputInvalidException("Cannot add a lesson to a published course.");
        }
        CourseSection section = findSectionById(sectionId);
        section.removeLesson(lessonId);
    };

    public void delete() {
        if (!isNotPublishedAndDeleted()) {
            throw new InputInvalidException("Cannot delete a published course.");
        }
        if (this.deleted) {
            throw new InputInvalidException("Course is already deleted.");
        }
        this.deleted = true;
    }

    public void deleteForce() {
        if (!this.deleted) {
            throw new InputInvalidException("Course is not deleted.");
        }
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

    public boolean isNotPublishedAndDeleted() {
        return !published && !deleted;
    }

    public boolean isPublishedAndNotDeleted() {
        return published && !deleted;
    }

    private boolean isValidCurrency(CurrencyUnit inputCurrency) {
        var validCurrencies = Set.of(Currencies.VND);
        return validCurrencies.contains(inputCurrency);
    }


}
