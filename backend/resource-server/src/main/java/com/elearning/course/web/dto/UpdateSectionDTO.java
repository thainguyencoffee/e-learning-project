package com.elearning.course.web.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateSectionDTO(
        @NotBlank(message = "Title must not be blank.")
        String title
) {
}
