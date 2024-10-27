package com.el.enrollment.domain;

import lombok.Getter;
import org.springframework.data.annotation.*;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table("course_enrollment")
@Getter
public class CourseEnrollment {
    @Id
    private Long id;
    private String student;
    private Long courseId;
    private Instant enrollmentDate;
    private Double progress;
    @CreatedBy
    private String createdBy;
    @CreatedDate
    private Instant createdDate;
    @LastModifiedBy
    private String lastModifiedBy;
    @LastModifiedDate
    private Instant lastModifiedDate;

    public CourseEnrollment(String student, Long courseId) {
        this.student = student;
        this.courseId = courseId;
        this.enrollmentDate = Instant.now();
        this.progress = 0.0;

    }

}
