package com.el.course.application.dto;

import com.el.course.domain.Language;

public record CourseInTrashDTO(
        Long id,
        String title,
        String thumbnailUrl,
        String description,
        String teacher,
        Language language
){
}
