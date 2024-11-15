package com.el.course.application.dto;

import com.el.common.ValidateMessages;
import com.el.course.domain.Lesson;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LessonDTO(
        @NotBlank(message = ValidateMessages.NOT_BLANK)
        String title,
        @NotNull(message = ValidateMessages.NOT_NULL)
        Lesson.Type type,
        String link
) {
    public Lesson toLesson() {
        return new Lesson(title, type, link);
    }
}