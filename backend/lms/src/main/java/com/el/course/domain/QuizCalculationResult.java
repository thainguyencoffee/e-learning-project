package com.el.course.domain;

public record QuizCalculationResult(
        Long quizId,
        Long afterLessonId,
        Integer score,
        boolean passed
) {
}
