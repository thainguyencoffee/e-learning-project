package com.elearning.course.application;

import com.elearning.common.UseCase;
import com.elearning.course.application.dto.CourseUpdateDTO;
import com.elearning.course.domain.Course;
import com.elearning.course.domain.CourseRepository;
import org.springframework.transaction.annotation.Transactional;

@UseCase
public class UpdateCourseUseCase {

    private final CourseRepository courseRepository;
    private final CourseQueryUseCase courseQueryUseCase;

    public UpdateCourseUseCase(CourseRepository courseRepository, CourseQueryUseCase courseQueryUseCase) {
        this.courseRepository = courseRepository;
        this.courseQueryUseCase = courseQueryUseCase;
    }

    @Transactional
    public Course execute(Long courseId, CourseUpdateDTO courseDTO) {
        Course course = courseQueryUseCase.findCourseById(courseId);
        course.updateInfo(
                courseDTO.title(),
                courseDTO.description(),
                courseDTO.thumbnailUrl(),
                courseDTO.benefits(),
                courseDTO.prerequisites(),
                courseDTO.subtitles());
        return courseRepository.save(course);
    }

}
