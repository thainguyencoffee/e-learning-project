package com.el.enrollment.application.dto;

import com.el.course.domain.Course;
import com.el.course.domain.CourseSection;
import com.el.course.domain.Language;
import com.el.course.domain.Post;
import com.el.enrollment.domain.Certificate;
import com.el.enrollment.domain.CourseEnrollment;
import com.el.enrollment.domain.LessonProgress;
import com.el.enrollment.domain.Progress;

import java.time.Instant;
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
        Set<Post> posts,
        String teacher,
        Long enrollmentId,
        String student,
        Set<LessonProgress> lessonProgresses,
        Boolean completed,
        Instant completedDate,
        Certificate certificate,
        Progress progress
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
                course.getSections(),
                course.getPosts(),
                course.getTeacher(),
                // Enrolment fields
                enrolment.getId(),
                enrolment.getStudent(),
                enrolment.getLessonProgresses(),
                enrolment.getCompleted(),
                enrolment.getCompletedDate(),
                enrolment.getCertificate(),
                enrolment.getProgress()
        );
    }
}
