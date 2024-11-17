package com.el.enrollment.application.dto;

import com.el.enrollment.domain.CourseEnrollment;

import java.time.LocalDateTime;
import java.util.List;

public record CourseInfoWithEnrolmentsDTO(
        Long courseId,
        String title,
        String thumbnailUrl,
        String teacher,
        List<CourseEnrollment> enrolments

) {

    public static CourseInfoWithEnrolmentsDTO of(CourseInfoDTO courseInfo, List<CourseEnrollment> enrollments) {
        return new CourseInfoWithEnrolmentsDTO(
                courseInfo.id(),
                courseInfo.title(),
                courseInfo.thumbnailUrl(),
                courseInfo.teacher(),
                enrollments
        );
    }
}
