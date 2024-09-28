package com.elearning.course.application.dto;

import com.elearning.course.domain.CourseSection;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.Set;

public record CourseSectionDTO(
        @NotBlank(message = "Section title is required")
        String title,
        @Valid
        Set<LessonDTO> lessons
) {

    public CourseSection toCourseSection() {
        CourseSection courseSection = new CourseSection(title);
        for (LessonDTO lessonDTO : lessons) {
            courseSection.addLesson(lessonDTO.toLesson());
        }
        return courseSection;
    }
}
