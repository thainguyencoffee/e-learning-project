package com.elearning.course.application;

import com.elearning.common.UseCase;
import com.elearning.course.domain.Course;
import com.elearning.course.domain.CourseRepository;
import org.springframework.transaction.annotation.Transactional;

@UseCase
public class RemoveSectionUseCase {

    private final CourseRepository courseRepository;
    private final CourseQueryUseCase courseQueryUseCase;

    public RemoveSectionUseCase(CourseRepository courseRepository, CourseQueryUseCase courseQueryUseCase) {
        this.courseRepository = courseRepository;
        this.courseQueryUseCase = courseQueryUseCase;
    }

    // Xóa một phần khỏi khóa học
    @Transactional
    public Course removeSection(Long courseId, Long sectionId) {
        Course course = courseQueryUseCase.findCourseById(courseId);
        course.removeSection(sectionId);
        return courseRepository.save(course);
    }

}
