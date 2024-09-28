package com.elearning.course.application.impl;

import com.elearning.common.exception.ResourceNotFoundException;
import com.elearning.course.application.CourseService;
import com.elearning.course.application.dto.CourseDTO;
import com.elearning.course.application.dto.CourseSectionDTO;
import com.elearning.course.application.dto.CourseUpdateDTO;
import com.elearning.course.domain.Course;
import com.elearning.course.domain.CourseRepository;
import com.elearning.course.domain.CourseSection;
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
        Course course = courseRepository.findById(courseId)
                .orElseThrow(ResourceNotFoundException::new);

        course.publish(approvedBy);
        return courseRepository.save(course);
    }

    @Override

    public void assignTeacher(Long courseId, String teacher) {
        findCourseById(courseId).assignTeacher(teacher);
    }

    @Override
    public Course applyDiscount(Long courseId, Long discountId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(ResourceNotFoundException::new);
        MonetaryAmount discountedPrice = discountService.calculateDiscount(discountId, course.getPrice());
        course.applyDiscount(discountedPrice, discountId);

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
    public Course removeSection(Long courseId, Long sectionId) {
        Course course = findCourseById(courseId);
        course.removeSection(sectionId);
        return courseRepository.save(course);
    }

    @Override
    public Course updatePrice(Long courseId, MonetaryAmount newPrice) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(ResourceNotFoundException::new);
        course.changePrice(newPrice);

        return courseRepository.save(course);
    }
}
