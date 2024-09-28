package com.elearning.course.application;

import com.elearning.common.UseCase;
import com.elearning.common.exception.ResourceNotFoundException;
import com.elearning.course.domain.Course;
import com.elearning.course.domain.CourseRepository;
import org.springframework.transaction.annotation.Transactional;

@UseCase
public class PublishCourseUseCase {

    private final CourseRepository courseRepository;

    public PublishCourseUseCase(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Transactional
    public Course execute(Long courseId, String approvedBy) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(ResourceNotFoundException::new);

        course.publish(approvedBy);
        return courseRepository.save(course);
    }

}
