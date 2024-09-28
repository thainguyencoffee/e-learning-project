package com.elearning.course.application.dto;

import com.elearning.course.domain.Course;
import com.elearning.course.domain.Language;
import jakarta.validation.constraints.NotBlank;

import java.util.Set;

public record CourseDTO (
        @NotBlank(message = "Title is required")
        String title,
        String description,
        String thumbnailUrl,
        Set<String> benefits,
        Language language,
        Set<String> prerequisites,
        Set<Language> subtitles
) {

    public Course toCourse(String teacher) {
        return new Course(
                title,
                description,
                thumbnailUrl,
                benefits,
                language,
                prerequisites,
                subtitles,
                teacher
        );
    }

}
