package com.el.course.application.dto;

public record QuizInTrashDTO(
        Long id,
        String title,
        String description,
        Long afterLessonId
) {
}
