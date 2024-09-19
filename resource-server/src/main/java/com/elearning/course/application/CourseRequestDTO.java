package com.elearning.course.application;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import javax.money.MonetaryAmount;
import java.util.Set;

public record CourseRequestDTO (
        @NotBlank(message = "Title is required")
        String title,
        MonetaryAmount price,
        String description,
        @Valid
        AudienceDTO audience,
        @Valid
        Set<CourseSectionDTO> sections,
        Long discountId
) {
}
