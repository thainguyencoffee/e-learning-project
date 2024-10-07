package com.el.course.domain;

import org.springframework.data.relational.core.mapping.Table;

@Table("course_student")
public record StudentRef(
        Long student,
        String firstName,
        String lastName,
        String email
) {
}
