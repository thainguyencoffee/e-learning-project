package com.elearning.course.application;

import com.elearning.common.UseCase;
import com.elearning.common.exception.ResourceNotFoundException;
import com.elearning.course.domain.Course;
import com.elearning.course.domain.CourseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@UseCase
public class CourseQueryUseCase {

    private final CourseRepository courseRepository;

    public CourseQueryUseCase(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public Page<Course> findAllCourses(Pageable pageable) {
        return courseRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Course findCourseById(Long courseId) {
        return courseRepository.findByIdAndDeleted(courseId, false)
                .orElseThrow(ResourceNotFoundException::new);
    }

}
