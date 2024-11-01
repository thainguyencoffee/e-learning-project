package com.el.course.web.dto;

import jakarta.validation.constraints.NotBlank;

public record AssignTeacherDTO(
        @NotBlank(message = "Teacher username is required")
        String teacher
) {
}
