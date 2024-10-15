package com.el.course.application.dto;

import com.el.course.domain.CourseSection;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record CourseSectionDTO(
        @NotBlank(message = "Section title is required")
        String title
//        @Valid
//        @NotEmpty(message = "At least one lesson is required")
//        Set<LessonDTO> lessons
) {

    public CourseSection toCourseSection() {
        CourseSection courseSection = new CourseSection(title);
//        for (LessonDTO lessonDTO : lessons) {
//            courseSection.addLesson(lessonDTO.toLesson());
//        }
        return courseSection;
    }
}
