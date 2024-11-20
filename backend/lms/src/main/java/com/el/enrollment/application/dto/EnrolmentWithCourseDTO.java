package com.el.enrollment.application.dto;

import com.el.course.domain.Course;
import com.el.course.domain.CourseSection;
import com.el.course.domain.Language;
import com.el.enrollment.domain.*;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Combination DTO class between {@link CourseEnrollment} and {@link Course}.
 * */
public record EnrolmentWithCourseDTO(
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
        Set<LessonProgress> lessonProgresses,
        Boolean completed,
        LocalDateTime completedDate,
        Certificate certificate,
        Progress progress,
        Set<QuizSubmission> quizSubmissions
) {
    public static EnrolmentWithCourseDTO of(CourseEnrollment enrolment, Course course) {
        return new EnrolmentWithCourseDTO(
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
                // Enrolment fields
                enrolment.getId(),
                enrolment.getStudent(),
                enrolment.getLessonProgresses(),
                enrolment.getCompleted(),
                enrolment.getCompletedDate(),
                enrolment.getCertificate(),
                enrolment.getProgress(),
                enrolment.getQuizSubmissions()
        );
    }
}
