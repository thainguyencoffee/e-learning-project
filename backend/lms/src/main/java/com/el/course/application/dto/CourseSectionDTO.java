package com.el.course.application.dto;

import com.el.common.ValidateMessages;
import com.el.course.domain.CourseSection;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CourseSectionDTO(
        @NotBlank(message = ValidateMessages.NOT_BLANK)
        @Size(max = 255, message = ValidateMessages.MAX_LENGTH)
        @Size(min = 10, message = ValidateMessages.MIN_LENGTH)
        String title
) {

    public CourseSection toCourseSection() {
        return new CourseSection(title);
    }
}
