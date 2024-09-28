package com.elearning.course.application;

import com.elearning.common.UseCase;
import com.elearning.course.application.dto.CourseSectionDTO;
import com.elearning.course.domain.Course;
import com.elearning.course.domain.CourseRepository;
import com.elearning.course.domain.CourseSection;
import org.springframework.transaction.annotation.Transactional;

@UseCase
public class AddSectionUseCase {

    private final CourseRepository courseRepository;
    private final CourseQueryUseCase courseQueryUseCase;

    public AddSectionUseCase(CourseRepository courseRepository, CourseQueryUseCase courseQueryUseCase) {
        this.courseRepository = courseRepository;
        this.courseQueryUseCase = courseQueryUseCase;
    }

    @Transactional
    public Course execute(Long courseId, CourseSectionDTO courseSectionDTO) {
        Course course = courseQueryUseCase.findCourseById(courseId);

        CourseSection courseSection = courseSectionDTO.toCourseSection();

        course.addSection(courseSection);
        return courseRepository.save(course);
    }

}
