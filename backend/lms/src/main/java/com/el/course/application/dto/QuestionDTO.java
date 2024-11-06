package com.el.course.application.dto;

import com.el.common.ValidateMessages;
import com.el.course.domain.AnswerOption;
import com.el.course.domain.Question;
import com.el.course.domain.QuestionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;
import java.util.stream.Collectors;

public record QuestionDTO(
        @NotBlank(message = ValidateMessages.NOT_BLANK)
        @Size(min = 10, message = ValidateMessages.MIN_LENGTH)
        @Size(max = 1000, message = ValidateMessages.MAX_LENGTH)
        String content,
        @NotNull(message = ValidateMessages.NOT_NULL)
        QuestionType type,
        @Valid
        Set<AnswerOptionDTO> options,
        @NotNull(message = ValidateMessages.NOT_NULL)
        Integer score
) {

    public Question toQuestion() {
        return new Question(content, type, score,
                options.stream()
                        .map(AnswerOptionDTO::toAnswerOption)
                        .collect(Collectors.toSet()));
    }

    public record AnswerOptionDTO(
            @NotBlank(message = ValidateMessages.NOT_BLANK)
            @Size(max = 1000, message = ValidateMessages.MAX_LENGTH)
            String content,
            @NotNull(message = ValidateMessages.NOT_NULL)
            Boolean correct
    ) {

        public AnswerOption toAnswerOption() {
            return new AnswerOption(content, correct);
        }
    }

}
