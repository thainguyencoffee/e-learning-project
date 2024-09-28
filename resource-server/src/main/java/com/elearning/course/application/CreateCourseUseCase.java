package com.elearning.course.application;

import com.elearning.common.UseCase;
import com.elearning.course.application.dto.CourseDTO;
import com.elearning.course.domain.Course;
import com.elearning.course.domain.CourseRepository;
import org.springframework.transaction.annotation.Transactional;

@UseCase
public class CreateCourseUseCase {

    private final CourseRepository courseRepository;

    public CreateCourseUseCase(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Transactional
    public Course execute(String teacherId, CourseDTO courseDTO) {

        Course course = courseDTO.toCourse(teacherId);
        return courseRepository.save(course);
    }

}
