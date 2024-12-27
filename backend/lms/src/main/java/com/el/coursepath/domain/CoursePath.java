package com.el.coursepath.domain;

import com.el.common.exception.InputInvalidException;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Table(name = "course_path")
@ToString
@Getter
public class CoursePath {
    @Id
    private Long id;
    private String title;
    private String description;
    @MappedCollection(idColumn = "course_path")
    private Set<CourseOrder> courseOrders = new HashSet<>();
    private String teacher;
    private boolean published;
    private LocalDateTime publishedDate;
    private boolean deleted;
    @Version
    private int version;
    @CreatedBy
    private String createdBy;
    @CreatedDate
    private LocalDateTime createdDate;
    @LastModifiedBy
    private String lastModifiedBy;
    @LastModifiedDate
    private LocalDateTime lastModifiedDate;

    public CoursePath(String title, String description, String teacher) {
        if (title == null || title.isEmpty())
            throw new InputInvalidException("Title is required. Please provide a title.");
        if (teacher == null || teacher.isEmpty())
            throw new InputInvalidException("Teacher is required. Please provide a teacher.");

        this.title = title;
        this.description = (description == null || description.isBlank()) ? "No description provided." : description;
        this.teacher = teacher;
        this.deleted = false;
        this.published = false;
    }

    public void updateInfo(String title, String description) {
        if (deleted)
            throw new InputInvalidException("Cannot update deleted course path.");
        if (published)
            throw new InputInvalidException("Cannot update published course path.");
        if (title == null || title.isEmpty())
            throw new InputInvalidException("Title is required. Please provide a title.");

        this.title = title;
        this.description = (description == null || description.isBlank()) ? "No description provided." : description;
    }

    public void delete() {
        if (deleted)
            throw new InputInvalidException("Course path is already deleted.");
        if (published)
            throw new InputInvalidException("Cannot delete published course path.");

        deleted = true;
    }

    public CourseOrder addCourseOrder(Long courseId) {
        if (deleted)
            throw new InputInvalidException("Cannot add course order to deleted course path.");
        if (published)
            throw new InputInvalidException("Cannot add course order to published course path.");
        boolean orderExists = courseOrders.stream()
                .anyMatch(co -> co.getCourseId().equals(courseId));
        if (orderExists)
            throw new InputInvalidException("Course order already exists for course ID: " + courseId);

        CourseOrder courseOrder = new CourseOrder(courseId, maxOrderInSet() + 1);
        courseOrders.add(courseOrder);
        return courseOrder;
    }

    private Integer maxOrderInSet() {
        return courseOrders.stream()
                .map(CourseOrder::getOrderIndex)
                .max(Integer::compareTo)
                .orElse(1);
    }

    public void removeCourseOrder(Long courseId) {
        if (deleted)
            throw new InputInvalidException("Cannot remove course order from deleted course path.");
        if (published)
            throw new InputInvalidException("Cannot remove course order from published course path.");
        courseOrders.removeIf(co -> co.getCourseId().equals(courseId));
    }

    public void publish() {
        if (deleted)
            throw new InputInvalidException("Cannot publish deleted course path.");
        if (courseOrders.isEmpty())
            throw new InputInvalidException("Cannot publish course path with no course orders.");
        if (published)
            throw new InputInvalidException("Course path is already published.");

        published = true;
        publishedDate = LocalDateTime.now();
    }

    public void unpublish() {
        if (deleted)
            throw new InputInvalidException("Cannot unpublish deleted course path.");
        if (!published)
            throw new InputInvalidException("Course path is not published.");

        published = false;
        publishedDate = null;
    }

    public void deleteForce() {
        if (!deleted)
            throw new InputInvalidException("Course path is not deleted.");
    }

    public void restore() {
        if (!deleted)
            throw new InputInvalidException("Course path is not deleted.");

        deleted = false;
    }
}
