package com.el.course.domain;

public record StudentsByCourseDTO(
        Long id,
        String title,
        String thumbnailUrl,
        Integer completedStudents,
        Integer totalStudents) {
}
