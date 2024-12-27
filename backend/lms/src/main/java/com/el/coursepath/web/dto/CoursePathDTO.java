package com.el.coursepath.web.dto;

import com.el.common.ValidateMessages;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CoursePathDTO(
        @NotBlank(message = ValidateMessages.NOT_BLANK)
        @Size(min = 20, message = ValidateMessages.MIN_LENGTH)
        String title,
        @Size(max = 2000, message = ValidateMessages.MAX_LENGTH)
        String description
) {
}
