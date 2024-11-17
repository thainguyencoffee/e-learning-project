package com.el.enrollment.application.dto;

import java.time.LocalDateTime;

public record CourseEnrollmentDTO(
        Long id,
        String student,
        Long courseId,
        String title,
        String thumbnailUrl,
        String teacher,
        LocalDateTime enrollmentDate,
        Boolean completed
) {
}
