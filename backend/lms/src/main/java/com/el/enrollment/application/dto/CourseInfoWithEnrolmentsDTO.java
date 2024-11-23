package com.el.enrollment.application.dto;

import com.el.enrollment.domain.Enrollment;

import java.util.List;

public record CourseInfoWithEnrolmentsDTO(
        Long courseId,
        String title,
        String thumbnailUrl,
        String teacher,
        List<Enrollment> enrolments

) {

    public static CourseInfoWithEnrolmentsDTO of(CourseInfoDTO courseInfo, List<Enrollment> enrollments) {
        return new CourseInfoWithEnrolmentsDTO(
                courseInfo.id(),
                courseInfo.title(),
                courseInfo.thumbnailUrl(),
                courseInfo.teacher(),
                enrollments
        );
    }
}
