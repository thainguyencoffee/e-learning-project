package com.el.enrollment.web.dto;

import com.el.common.ValidateMessages;
import com.el.course.domain.QuestionType;
import com.el.enrollment.web.validate.QuestionSubmitConstraint;
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
        Boolean trueFalseAnswer,
        Long singleChoiceAnswer
) {
    public QuizAnswer toQuizAnswer() {
        if (type == QuestionType.TRUE_FALSE) {
            return new QuizAnswer(questionId(), trueFalseAnswer(), type());
        }
        if (type == QuestionType.SINGLE_CHOICE) {
            return new QuizAnswer(questionId(), singleChoiceAnswer, type());
        } else {
            return new QuizAnswer(questionId(), answerOptionIds(), type());
        }
    }
}
