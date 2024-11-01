package com.el.course.application.dto;

import com.el.course.domain.Language;

import javax.money.MonetaryAmount;
import java.util.Set;

public record CourseWithoutSectionsDTO(
        Long id,
        String title,
        String thumbnailUrl,
        String description,
        Language language,
        Set<Language> subtitles,
        Set<String> benefits,
        Set<String> prerequisites,
        MonetaryAmount price,
        String teacher
) {
}
