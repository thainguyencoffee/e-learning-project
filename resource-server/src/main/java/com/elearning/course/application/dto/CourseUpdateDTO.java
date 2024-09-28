package com.elearning.course.application.dto;

import com.elearning.course.domain.Language;
import jakarta.validation.constraints.NotBlank;

import java.util.Set;

public record CourseUpdateDTO(
        @NotBlank(message = "Title is required")
        String title,
        String description,
        String thumbnailUrl,
        Set<String> benefits,
        Set<String> prerequisites,
        Set<Language> subtitles
) {

}
