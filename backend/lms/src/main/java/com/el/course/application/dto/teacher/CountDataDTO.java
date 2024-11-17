package com.el.course.application.dto.teacher;

public record CountDataDTO(
        String teacher,
        Integer numberOfCourses,
        Integer numberOfStudents,
        Integer numberOfCertificates,
        Integer numberOfDraftCourses
) {
}
