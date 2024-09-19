package com.elearning.course.application;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LessonDTO(
        Long id,
        @NotBlank(message = "Lesson title is required")
        String title,
        @NotBlank(message = "Lesson link is required")
        String link,
        @NotBlank(message = "Lesson type is required")
        @Pattern(regexp = "VIDEO|TEXT|QUIZ|ASSIGNMENT", message = "Invalid lesson type")
        String type
) {
}