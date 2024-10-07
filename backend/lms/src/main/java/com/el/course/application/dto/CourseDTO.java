package com.el.course.application.dto;

import com.el.course.application.validate.EachItemStringMaxSize;
import com.el.course.domain.Course;
import com.el.course.domain.Language;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record CourseDTO (
        @NotBlank(message = "Title is required")
        String title,
        @Size(max = 2000, message = "Description is too long")
        String description,
        String thumbnailUrl,
        @EachItemStringMaxSize(max = 255, message = "Benefit is too long")
        Set<String> benefits,
        Language language,
        @EachItemStringMaxSize(max = 255, message = "Prerequisite is too long")
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
