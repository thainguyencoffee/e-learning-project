package com.el.enrollment.application.dto;

public record CourseInfoDTO(
        Long id,
        String title,
        String thumbnailUrl,
        String teacher
) {
}
