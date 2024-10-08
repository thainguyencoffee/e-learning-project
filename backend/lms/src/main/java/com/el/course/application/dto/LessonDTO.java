package com.el.course.application.dto;

import com.el.course.domain.Lesson;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LessonDTO(
        @NotBlank(message = "Lesson title is required")
        String title,
        @NotNull(message = "Lesson description is required")
        Lesson.Type type,
        String link,
        Long quiz
) {
    public Lesson toLesson() {
        return new Lesson(title, type, link, quiz);
    }
}