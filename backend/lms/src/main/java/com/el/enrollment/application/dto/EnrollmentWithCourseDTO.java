package com.el.enrollment.application.dto;

import com.el.course.domain.Course;
import com.el.course.domain.CourseSection;
import com.el.course.domain.Language;
import com.el.enrollment.domain.*;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Combination DTO class between {@link Enrollment} and {@link Course}.
 * */
public record EnrollmentWithCourseDTO(
        Long courseId,
        String title,
        String thumbnailUrl,
        String description,
        Language language,
        Set<Language> subtitles,
        Set<String> benefits,
        Set<String> prerequisites,
        Set<CourseSection> sections,
        String teacher,
        Long enrollmentId,
        String student,
        LocalDateTime enrollmentDate,
        Set<LessonProgress> lessonProgresses,
        Boolean completed,
        Boolean reviewed,
        LocalDateTime completedDate,
        Certificate certificate,
        Progress progress,
        Set<QuizSubmission> quizSubmissions
) {
    public static EnrollmentWithCourseDTO of(Enrollment enrollment, Course course) {
        return new EnrollmentWithCourseDTO(
                // Course fields
                course.getId(),
                course.getTitle(),
                course.getThumbnailUrl(),
                course.getDescription(),
                course.getLanguage(),
                course.getSubtitles(),
                course.getBenefits(),
                course.getPrerequisites(),
                course.getSectionForPublished(),
                course.getTeacher(),
                // Enrollment fields
                enrollment.getId(),
                enrollment.getStudent(),
                enrollment.getEnrollmentDate(),
                enrollment.getLessonProgresses(),
                enrollment.getCompleted(),
                enrollment.getReviewed(),
                enrollment.getCompletedDate(),
                enrollment.getCertificate(),
                enrollment.getProgress(),
                enrollment.getQuizSubmissions()
        );
    }
}
