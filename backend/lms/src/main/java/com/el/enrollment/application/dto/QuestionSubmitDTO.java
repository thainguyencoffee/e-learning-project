package com.el.enrollment.application.dto;

import com.el.common.ValidateMessages;
import com.el.course.domain.QuestionType;
import com.el.enrollment.application.validate.QuestionSubmitConstraint;
import com.el.enrollment.domain.QuizAnswer;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

@QuestionSubmitConstraint
public record QuestionSubmitDTO(
        @NotNull(message = ValidateMessages.NOT_NULL)
        QuestionType type,
        @NotNull(message = ValidateMessages.NOT_NULL)
        Long questionId,
        Set<Long> answerOptionIds,
        Boolean trueFalseAnswer
) {
        public QuizAnswer toQuizAnswer() {
                if (type == QuestionType.TRUE_FALSE) {
                        return new QuizAnswer(questionId(), trueFalseAnswer(), type());
                }
                if (type == QuestionType.SINGLE_CHOICE) {
                        return new QuizAnswer(questionId(), answerOptionIds().iterator().next(), type());
                }
                return new QuizAnswer(questionId(), answerOptionIds(), type());
        }
}
