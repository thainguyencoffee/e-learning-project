package com.el.course.web.dto;

import jakarta.validation.constraints.NotBlank;

public record AssignTeacherDTO(
        @NotBlank(message = "Teacher ID is required")
        String teacherId
) {
}
