package com.el.enrollment.web.dto;

import com.el.common.ValidateMessages;
import com.el.course.domain.QuestionType;
import com.el.enrollment.domain.QuizAnswer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public record QuizSubmitDTO(
        @NotNull(message = ValidateMessages.NOT_NULL)
        Long quizId,
        @Valid
        Set<QuestionSubmitDTO> questions
) {

    // This method used to prepare data to calculate
    @JsonIgnore
    public Map<Long, Object> getAnswers() {
        return questions.stream()
                .collect(HashMap::new, (map, question) -> {
                    if (question.type() == QuestionType.TRUE_FALSE) {
                        if (question.trueFalseAnswer() != null) {
                            map.put(question.questionId(), question.trueFalseAnswer());
                        }
                    } else if (question.type() == QuestionType.MULTIPLE_CHOICE) {
                        map.put(question.questionId(), question.answerOptionIds());
                    } else {
                        map.put(question.questionId(), question.singleChoiceAnswer());
                    }
                }, Map::putAll);
    }

    // This method used to prepare data to save in database
    @JsonIgnore
    public Set<QuizAnswer> toQuizAnswers() {
        return questions.stream()
                .map(QuestionSubmitDTO::toQuizAnswer)
                .collect(Collectors.toSet());
    }

}
