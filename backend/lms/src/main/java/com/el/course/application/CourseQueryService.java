package com.el.course.application;

import com.el.course.domain.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CourseQueryService {

    Page<Course> findAllCourses(Pageable pageable);

    Course findCourseById(Long courseId);

    Page<Course> findAllPublishedCourses(Pageable pageable);

    Course findPublishedCourseById(Long courseId);

    Course findCourseDeleted(Long courseId);

}
