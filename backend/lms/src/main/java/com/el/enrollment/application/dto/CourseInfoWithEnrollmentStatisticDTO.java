package com.el.enrollment.application.dto;

public record CourseInfoWithEnrollmentStatisticDTO(
        Long courseId,
        String title,
        String thumbnailUrl,
        String teacher,
        Long totalEnrollments,
        Long totalCompletedEnrollments
) {
}
