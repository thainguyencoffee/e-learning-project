package com.el.enrollment.application;

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
