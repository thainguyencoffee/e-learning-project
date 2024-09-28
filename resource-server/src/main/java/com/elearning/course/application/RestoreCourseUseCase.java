package com.elearning.course.application;

import com.elearning.common.UseCase;
import com.elearning.course.domain.Course;
import com.elearning.course.domain.CourseRepository;

@UseCase
public class RestoreCourseUseCase {

    private final CourseQueryUseCase courseQueryUseCase;
    private final CourseRepository courseRepository;

    public RestoreCourseUseCase(CourseQueryUseCase courseQueryUseCase, CourseRepository courseRepository) {
        this.courseQueryUseCase = courseQueryUseCase;
        this.courseRepository = courseRepository;
    }

    public void execute (Long courseId) {
        Course course = courseQueryUseCase.findCourseById(courseId);
        course.restore();
        courseRepository.save(course);
    }

}
