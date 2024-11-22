package com.el.course.web.dto;

import com.el.common.ValidateMessages;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record QuizUpdateDTO(
        @NotBlank(message = ValidateMessages.NOT_BLANK)
        @Size(min = 10, message = ValidateMessages.MIN_LENGTH)
        @Size(max = 255, message = ValidateMessages.MAX_LENGTH)
        String title,
        @Size(max = 2000, message = ValidateMessages.MAX_LENGTH)
        String description,
        @NotNull(message = ValidateMessages.NOT_NULL)
        Integer passScorePercentage
) {
}
