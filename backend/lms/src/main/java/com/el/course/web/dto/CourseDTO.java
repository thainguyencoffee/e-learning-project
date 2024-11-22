package com.el.course.web.dto;

import com.el.common.ValidateMessages;
import com.el.course.web.validate.EachItemStringMaxSize;
import com.el.course.domain.Course;
import com.el.course.domain.Language;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record CourseDTO(
        @NotBlank(message = ValidateMessages.NOT_BLANK)
        @Size(max = 255, message = ValidateMessages.MAX_LENGTH)
        @Size(min = 10, message = ValidateMessages.MIN_LENGTH)
        String title,
        @Size(max = 2000, message = ValidateMessages.MAX_LENGTH)
        String description,
        String thumbnailUrl,
        @EachItemStringMaxSize(max = 255)
        Set<String> benefits,
        Language language,
        @EachItemStringMaxSize(max = 255)
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
