package com.el.coursepath.web.dto;

import com.el.common.ValidateMessages;
import jakarta.validation.constraints.NotNull;

public record CourseOrderDTO(
        @NotNull(message = ValidateMessages.NOT_NULL)
        Long courseId
) {
}
