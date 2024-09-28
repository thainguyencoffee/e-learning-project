package com.elearning.course.application.dto;

import com.elearning.course.application.validate.EachItemStringMaxSize;
import com.elearning.course.domain.Language;
import jakarta.validation.constraints.NotBlank;

import java.util.Set;

public record CourseUpdateDTO(
        @NotBlank(message = "Title is required")
        String title,
        String description,
        String thumbnailUrl,
        @EachItemStringMaxSize(max = 255, message = "Benefit is too long")
        Set<String> benefits,
        @EachItemStringMaxSize(max = 255, message = "Prerequisite is too long")
        Set<String> prerequisites,
        Set<Language> subtitles
) {

}
