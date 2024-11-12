package com.el.course.web.dto;

import com.el.common.ValidateMessages;
import com.el.course.domain.QuestionType;
import com.el.course.web.validate.QuestionSubmitConstraint;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@QuestionSubmitConstraint
public record QuestionSubmitDTO(
        @NotNull(message = ValidateMessages.NOT_NULL)
        QuestionType type,
        @NotNull(message = ValidateMessages.NOT_NULL)
        Long questionId,
        @NotNull(message = ValidateMessages.NOT_NULL)
        @NotEmpty
        List<Long> answerOptionIds
) {
}
