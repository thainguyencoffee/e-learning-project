package com.el.enrollment.domain;

import com.el.common.exception.InputInvalidException;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table("certificate")
@Getter
public class Certificate {
    @Id
    private UUID id;
    private String fullName;
    private String email;
    private String student;
    private String teacher;
    private String url;
    private Long courseId;
    private String courseTitle;
    private LocalDateTime issuedDate;
    private boolean certified;

    public Certificate(String fullName, String email, String student, Long courseId, String courseTitle, String teacher) {
        if (fullName == null || fullName.isBlank()) throw new InputInvalidException("Full name must not be null or empty.");
        if (email == null || email.isBlank()) throw new InputInvalidException("Email must not be null or empty.");
        if (student == null || student.isBlank()) throw new InputInvalidException("Student must not be null or empty.");
        if (courseId == null) throw new InputInvalidException("CourseId must not be null.");
        if (courseTitle == null || courseTitle.isBlank()) throw new InputInvalidException("Title must not be null or empty.");
        if (teacher == null || teacher.isBlank()) throw new InputInvalidException("Teacher must not be null or empty.");

        this.fullName = fullName;
        this.email = email;
        this.student = student;
        this.courseId = courseId;
        this.courseTitle = courseTitle;
        this.teacher = teacher;

        // Default values
        this.certified = false;
        this.issuedDate = LocalDateTime.now();
    }

    public void markAsCertified(String url) {
        if (url == null || url.isBlank()) throw new InputInvalidException("Url must not be null or empty.");
        if (certified) throw new InputInvalidException("Certificate is already certified.");
        this.url = url;
        this.certified = true;
    }

}
