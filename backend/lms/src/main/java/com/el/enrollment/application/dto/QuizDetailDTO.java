package com.el.enrollment.application.dto;

import com.el.course.domain.AnswerOption;
import com.el.course.domain.Question;
import com.el.course.domain.Quiz;

import java.util.List;

public record QuizDetailDTO(
        Long id,
        String title,
        String description,
        Long afterLessonId,
        List<QuestionDTO> questions,
        Integer totalScore,
        Integer passScorePercentage
) {

    public static QuizDetailDTO fromQuiz(Quiz quiz) {
        return new QuizDetailDTO(
                quiz.getId(),
                quiz.getTitle(),
                quiz.getDescription(),
                quiz.getAfterLessonId(),
                quiz.getQuestions().stream().map(QuestionDTO::fromQuestion).toList(),
                quiz.getTotalScore(),
                quiz.getPassScorePercentage()
        );
    }

    public record QuestionDTO(
            Long id,
            String content,
            String type,
            List<AnswerOptionDTO> options,
            Boolean trueFalseAnswer,
            Integer score
    ) {
        public static QuestionDTO fromQuestion(Question question) {
            return new QuestionDTO(
                    question.getId(),
                    question.getContent(),
                    question.getType().name(),
                    question.getOptions().stream().map(AnswerOptionDTO::fromAnswerOption).toList(),
                    question.getTrueFalseAnswer(),
                    question.getScore()
            );
        }
    }

    public record AnswerOptionDTO(
            Long id,
            String content
    ) {
        public static AnswerOptionDTO fromAnswerOption(AnswerOption answerOption) {
            return new AnswerOptionDTO(
                    answerOption.getId(),
                    answerOption.getContent()
            );
        }
    }

}



