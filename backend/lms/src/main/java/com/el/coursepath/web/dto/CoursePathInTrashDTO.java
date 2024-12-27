package com.el.coursepath.web.dto;

import com.el.coursepath.domain.CoursePath;

public record CoursePathInTrashDTO(
        Long id,
        String title,
        String description,
        String teacher
) {
    public static CoursePathInTrashDTO fromCoursePath(CoursePath coursePath) {
        return new CoursePathInTrashDTO(
                coursePath.getId(),
                coursePath.getTitle(),
                coursePath.getDescription(),
                coursePath.getTeacher()
        );
    }
}
