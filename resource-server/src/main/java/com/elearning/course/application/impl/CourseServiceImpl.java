package com.elearning.course.application.impl;

import com.elearning.common.exception.InputInvalidException;
import com.elearning.common.exception.ResourceNotFoundException;
import com.elearning.course.application.CourseService;
import com.elearning.course.application.dto.CourseDTO;
import com.elearning.course.application.dto.CourseSectionDTO;
import com.elearning.course.application.dto.CourseUpdateDTO;
import com.elearning.course.domain.Course;
import com.elearning.course.domain.CourseRepository;
import com.elearning.course.domain.CourseSection;
import com.elearning.course.domain.Lesson;
import com.elearning.discount.application.DiscountService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.money.MonetaryAmount;

@Service
@Transactional
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final DiscountService discountService;

    public CourseServiceImpl(CourseRepository courseRepository, DiscountService discountService) {
        this.courseRepository = courseRepository;
        this.discountService = discountService;
    }

    @Override
    public Page<Course> findAllCourses(Pageable pageable) {
        return courseRepository.findAll(pageable);
    }

    @Override
    public Course findCourseById(Long courseId) {
        return courseRepository.findByIdAndDeleted(courseId, false)
                .orElseThrow(ResourceNotFoundException::new);
    }

    @Override
    public Course findCourseDeleted(Long courseId) {
        return courseRepository.findByIdAndDeleted(courseId, true)
                .orElseThrow(ResourceNotFoundException::new);
    }

    @Override
    public Course createCourse(String teacherId, CourseDTO courseDTO) {
        Course course = courseDTO.toCourse(teacherId);
        return courseRepository.save(course);
    }

    @Override
    public Course updateCourse(Long courseId, CourseUpdateDTO courseUpdateDTO) {
        Course course = findCourseById(courseId);
        course.updateInfo(
                courseUpdateDTO.title(),
                courseUpdateDTO.description(),
                courseUpdateDTO.thumbnailUrl(),
                courseUpdateDTO.benefits(),
                courseUpdateDTO.prerequisites(),
                courseUpdateDTO.subtitles());
        return courseRepository.save(course);
    }

    @Override
    public void deleteCourse(Long courseId) {
        Course course = findCourseById(courseId);
        course.delete();
        courseRepository.save(course);
    }

    @Override
    public void restoreCourse(Long courseId) {
        Course course = findCourseDeleted(courseId);
        course.restore();
        courseRepository.save(course);
    }

    @Override
    public Course publishCourse(Long courseId, String approvedBy) {
        Course course = findCourseById(courseId);

        course.publish(approvedBy);
        return courseRepository.save(course);
    }

    @Override
    public Course assignTeacher(Long courseId, String teacher) {
        Course existsCourse = findCourseById(courseId);
        existsCourse.assignTeacher(teacher);
        courseRepository.save(existsCourse);
        return existsCourse;
    }

    @Override
    public Course applyDiscount(Long courseId, String code) {
        Course course = findCourseById(courseId);
        if (course.getPrice() == null) {
            throw new InputInvalidException("Cannot apply discount to a course without a price.");
        }
        MonetaryAmount discountedPrice = discountService.calculateDiscount(code, course.getPrice());
        course.applyDiscount(discountedPrice, code);

        return courseRepository.save(course);
    }

    @Override
    public Course addSection(Long courseId, CourseSectionDTO courseSectionDTO) {
        Course course = findCourseById(courseId);

        CourseSection courseSection = courseSectionDTO.toCourseSection();

        course.addSection(courseSection);
        return courseRepository.save(course);
    }

    @Override
    public Course updateSectionInfo(Long courseId, Long sectionId, String newTitle) {
        Course course = findCourseById(courseId);
        course.updateSection(sectionId, newTitle);
        return courseRepository.save(course);
    }

    @Override
    public Course removeSection(Long courseId, Long sectionId) {
        Course course = findCourseById(courseId);
        course.removeSection(sectionId);
        return courseRepository.save(course);
    }

    @Override
    public Course addLesson(Long courseId, Long sectionId, Lesson lesson) {
        Course course = findCourseById(courseId);
        course.addLessonToSection(sectionId, lesson);
        return courseRepository.save(course);
    }

    @Override
    public Course updateLesson(Long courseId, Long sectionId, Long lessonId, Lesson updatedLesson) {
        Course course = findCourseById(courseId);
        course.updateLessonInSection(sectionId, lessonId, updatedLesson);
        return courseRepository.save(course);
    }

    @Override
    public Course updatePrice(Long courseId, MonetaryAmount newPrice) {
        Course course = findCourseById(courseId);
        course.changePrice(newPrice);

        return courseRepository.save(course);
    }
}
