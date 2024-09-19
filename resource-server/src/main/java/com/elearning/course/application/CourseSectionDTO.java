package com.elearning.course.application;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.Set;

public record CourseSectionDTO(
        Long id,
        @NotBlank(message = "Section title is required")
        String title,
        String description,
        @Valid
        Set<LessonDTO> lessons
) {
}
