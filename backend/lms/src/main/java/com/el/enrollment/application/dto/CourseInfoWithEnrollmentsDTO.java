package com.el.enrollment.application.dto;

import com.el.enrollment.domain.Enrollment;

import java.util.List;

public record CourseInfoWithEnrollmentsDTO(
        Long courseId,
        String title,
        String thumbnailUrl,
        String teacher,
        List<Enrollment> enrollments

) {

    public static CourseInfoWithEnrollmentsDTO of(CourseInfoDTO courseInfo, List<Enrollment> enrollments) {
        return new CourseInfoWithEnrollmentsDTO(
                courseInfo.id(),
                courseInfo.title(),
                courseInfo.thumbnailUrl(),
                courseInfo.teacher(),
                enrollments
        );
    }
}
