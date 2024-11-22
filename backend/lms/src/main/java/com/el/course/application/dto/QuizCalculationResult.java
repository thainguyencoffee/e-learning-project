package com.el.course.application.dto;

public record QuizCalculationResult(
        Long quizId,
        Long afterLessonId,
        Integer score,
        boolean passed
) {
}
