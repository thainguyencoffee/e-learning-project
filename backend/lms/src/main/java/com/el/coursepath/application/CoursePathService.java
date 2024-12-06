package com.el.coursepath.application;

import com.el.coursepath.web.dto.CoursePathDTO;

public interface CoursePathService {

    Long createCoursePath(CoursePathDTO coursePathDTO);

    void updateCoursePath(Long coursePathId, CoursePathDTO coursePathDTO);

    void deleteCoursePath(Long coursePathId);

    void deleteCoursePathForce(Long coursePathId);

    void restoreCoursePath(Long coursePathId);

    Long addCourseOrder(Long coursePathId, Long courseId);

    void removeCourseOrder(Long coursePathId, Long courseOrderId);

    void publishCoursePath(Long coursePathId);

    void unpublishCoursePath(Long coursePathId);

}
