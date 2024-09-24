package com.elearning.course.application;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import javax.money.MonetaryAmount;
import java.util.Set;

public record CourseRequestDTO (
        @NotBlank(message = "Title is required")
        String title,
        @NotNull(message = "Price is required")
        MonetaryAmount price,
        String description,
        @Valid
        AudienceDTO audience,
        @Valid
        Set<CourseSectionDTO> sections,
        Long discountId,
        @NotBlank(message = "Thumbnail url is required")
        String thumbnailUrl
) {
}
