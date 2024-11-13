package com.el.enrollment.application.dto;

import com.el.common.ValidateMessages;
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

        @JsonIgnore
        public Map<Long, Set<Long>> getAnswers() {
                return questions.stream()
                        .collect(HashMap::new, (map, question) -> map.put(question.questionId(), question.answerOptionIds()), Map::putAll);
        }

        @JsonIgnore
        public Set<QuizAnswer> toQuizAnswers() {
                return questions.stream()
                        .map(QuestionSubmitDTO::toQuizAnswer)
                        .collect(Collectors.toSet());
        }

}
