package com.el.enrollment.application.dto;

import java.time.Instant;

public record CourseEnrollmentDTO(
        Long id,
        String student,
        Long courseId,
        String title,
        String thumbnailUrl,
        String teacher,
        Instant enrollmentDate,
        Boolean completed
) {
}
