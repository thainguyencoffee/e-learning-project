package com.elearning.course.domain;

import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("course_student")
public record StudentRef(
        Long student,
        String firstName,
        String lastName,
        String email
) {
}
