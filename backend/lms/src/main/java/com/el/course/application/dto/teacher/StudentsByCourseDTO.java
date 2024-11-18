package com.el.course.application.dto.teacher;

public record StudentsByCourseDTO(
        Long id,
        String title,
        String thumbnailUrl,
        Integer completedStudents,
        Integer totalStudents) {
}
