package com.el.enrollment.domain;

public record Progress(
        int totalLessons,
        int completedLessons,
        int totalQuizzes,
        int passedQuizzes,
        int totalLessonBonus,
        int totalQuizBonus,
        double progressPercentage
) {
}
