package com.elearning.course.application;

import com.elearning.common.UseCase;
import com.elearning.course.domain.Course;
import com.elearning.course.domain.CourseRepository;
import org.springframework.transaction.annotation.Transactional;

@UseCase
public class DeleteCourseUseCase {

    private final CourseRepository courseRepository;
    private final CourseQueryUseCase courseQueryUseCase;

    public DeleteCourseUseCase(CourseRepository courseRepository, CourseQueryUseCase courseQueryUseCase) {
        this.courseRepository = courseRepository;
        this.courseQueryUseCase = courseQueryUseCase;
    }

    @Transactional
    public void execute(Long courseId) {
        Course course = courseQueryUseCase.findCourseById(courseId);
        course.delete();
        courseRepository.save(course);
    }

}
