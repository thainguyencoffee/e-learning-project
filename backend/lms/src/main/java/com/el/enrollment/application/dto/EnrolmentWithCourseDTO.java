package com.el.enrollment.application.dto;

import com.el.course.domain.Course;
import com.el.course.domain.CourseSection;
import com.el.course.domain.Language;
import com.el.enrollment.domain.CourseEnrollment;
import com.el.enrollment.domain.LessonProgress;

import java.util.Set;

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
        Boolean completed
) {
    public static EnrolmentWithCourseDTO of(CourseEnrollment enrolment, Course course) {
        return new EnrolmentWithCourseDTO(
                course.getId(),
                course.getTitle(),
                course.getThumbnailUrl(),
                course.getDescription(),
                course.getLanguage(),
                course.getSubtitles(),
                course.getBenefits(),
                course.getPrerequisites(),
                course.getSections(),
                course.getTeacher(),
                enrolment.getId(),
                enrolment.getStudent(),
                enrolment.getLessonProgresses(),
                enrolment.getCompleted()
        );
    }
}
