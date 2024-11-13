package com.el.enrollment.domain;

import com.el.common.exception.InputInvalidException;
import com.el.course.domain.QuestionType;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.HashSet;
import java.util.Set;

@Table("quiz_answer")
@Getter
public class QuizAnswer {
    @Id
    private Long id;
    private Long questionId;
    private Set<Long> answerOptionIds = new HashSet<>();
    private QuestionType type;

    public QuizAnswer(Long questionId, Long answerOptionId, QuestionType type) {
        validateQuestionId(questionId);
        if (type == null) {
            throw new InputInvalidException("Question type cannot be null.");
        }
        this.type = type;
        addSingleAnswer(questionId, answerOptionId);
    }

    public QuizAnswer(Long questionId, Set<Long> answerOptionIds, QuestionType type) {
        validateQuestionId(questionId);
        validateAnswerOptionIds(answerOptionIds);
        if (type == null) {
            throw new InputInvalidException("Question type cannot be null.");
        }
        if (type == QuestionType.TRUE_FALSE || type == QuestionType.SINGLE_CHOICE) {
            throw new InputInvalidException("Type for QuizAnswer multiple choice signature is invalid.");
        }
        this.type = type;
        this.questionId = questionId;
        this.answerOptionIds.addAll(answerOptionIds);
    }


    private void addSingleAnswer(Long optionId, Long answerOptionId) {
        validateAnswerOptionIds(Set.of(answerOptionId));
        this.questionId = optionId;
        answerOptionIds.add(answerOptionId);
    }

    private void validateQuestionId(Long questionId) {
        if (questionId == null) {
            throw new InputInvalidException("Question ID cannot be null.");
        }
    }

    private void validateAnswerOptionIds(Set<Long> answerOptionIds) {
        if (answerOptionIds == null || answerOptionIds.isEmpty()) {
            throw new InputInvalidException("Answer option IDs cannot be null or empty.");
        }
        if (!this.answerOptionIds.isEmpty()) {
            throw new InputInvalidException("Answer options already set; cannot modify.");
        }
    }

}
