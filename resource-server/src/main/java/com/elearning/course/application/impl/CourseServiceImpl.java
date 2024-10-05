package com.elearning.course.application.impl;

import com.elearning.common.RolesBaseUtil;
import com.elearning.common.exception.AccessDeniedException;
import com.elearning.common.exception.InputInvalidException;
import com.elearning.course.application.CourseQueryService;
import com.elearning.course.application.CourseService;
import com.elearning.course.application.dto.CourseDTO;
import com.elearning.course.application.dto.CourseSectionDTO;
import com.elearning.course.application.dto.CourseUpdateDTO;
import com.elearning.course.domain.Course;
import com.elearning.course.domain.CourseRepository;
import com.elearning.course.domain.CourseSection;
import com.elearning.course.domain.Lesson;
import com.elearning.discount.application.DiscountService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.money.MonetaryAmount;

@Service
@Transactional
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final CourseQueryService courseQueryService;
    private final DiscountService discountService;
    private final RolesBaseUtil rolesBaseUtil;

    public CourseServiceImpl(CourseRepository courseRepository,
                             CourseQueryService courseQueryService,
                             DiscountService discountService,
                             RolesBaseUtil rolesBaseUtil) {
        this.courseRepository = courseRepository;
        this.courseQueryService = courseQueryService;
        this.discountService = discountService;
        this.rolesBaseUtil = rolesBaseUtil;
    }

    @Override
    public Course createCourse(String teacherId, CourseDTO courseDTO) {
        Course course = courseDTO.toCourse(teacherId);
        return courseRepository.save(course);
    }

    @Override
    public Course updateCourse(Long courseId, CourseUpdateDTO courseUpdateDTO) {
        Course course = courseQueryService.findCourseById(courseId);

        if (!canUpdateCourse(course)) {
            throw new AccessDeniedException("You do not have permission to update this course");
        }

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
        Course course = courseQueryService.findCourseById(courseId);

        if (!canUpdateCourse(course)) {
            throw new AccessDeniedException("You do not have permission to update this course");
        }

        course.delete();
        courseRepository.save(course);
    }

    @Override
    public void restoreCourse(Long courseId) {
        Course course = courseQueryService.findCourseDeleted(courseId);

        if (!canUpdateCourse(course)) {
            throw new AccessDeniedException("You do not have permission to update this course");
        }

        course.restore();
        courseRepository.save(course);
    }

    @Override
    public Course publishCourse(Long courseId, String approvedBy) {
        Course course = courseQueryService.findCourseById(courseId);

        course.publish(approvedBy);
        return courseRepository.save(course);
    }

    @Override
    public Course assignTeacher(Long courseId, String teacher) {
        Course existsCourse = courseQueryService.findCourseById(courseId);
        existsCourse.assignTeacher(teacher);
        courseRepository.save(existsCourse);
        return existsCourse;
    }

    @Override
    public Course applyDiscount(Long courseId, String code) {
        Course course = courseQueryService.findCourseById(courseId);
        if (course.getPrice() == null) {
            throw new InputInvalidException("Cannot apply discount to a course without a price.");
        }
        MonetaryAmount discountedPrice = discountService.calculateDiscount(code, course.getPrice());
        course.applyDiscount(discountedPrice, code);

        return courseRepository.save(course);
    }

    @Override
    public Course addSection(Long courseId, CourseSectionDTO courseSectionDTO) {
        Course course = courseQueryService.findCourseById(courseId);

        if (!canUpdateCourse(course)) {
            throw new AccessDeniedException("You do not have permission to update this course");
        }

        CourseSection courseSection = courseSectionDTO.toCourseSection();

        course.addSection(courseSection);
        return courseRepository.save(course);
    }

    @Override
    public Course updateSectionInfo(Long courseId, Long sectionId, String newTitle) {
        Course course = courseQueryService.findCourseById(courseId);

        if (!canUpdateCourse(course)) {
            throw new AccessDeniedException("You do not have permission to update this course");
        }

        course.updateSection(sectionId, newTitle);
        return courseRepository.save(course);
    }

    @Override
    public Course removeSection(Long courseId, Long sectionId) {
        Course course = courseQueryService.findCourseById(courseId);

        if (!canUpdateCourse(course)) {
            throw new AccessDeniedException("You do not have permission to update this course");
        }

        course.removeSection(sectionId);
        return courseRepository.save(course);
    }

    @Override
    public Course addLesson(Long courseId, Long sectionId, Lesson lesson) {
        Course course = courseQueryService.findCourseById(courseId);

        if (!canUpdateCourse(course)) {
            throw new AccessDeniedException("You do not have permission to update this course");
        }

        course.addLessonToSection(sectionId, lesson);
        return courseRepository.save(course);
    }

    @Override
    public Course updateLesson(Long courseId, Long sectionId, Long lessonId, Lesson updatedLesson) {
        Course course = courseQueryService.findCourseById(courseId);

        if (!canUpdateCourse(course)) {
            throw new AccessDeniedException("You do not have permission to update this course");
        }

        course.updateLessonInSection(sectionId, lessonId, updatedLesson);
        return courseRepository.save(course);
    }

    @Override
    public Course removeLesson(Long courseId, Long sectionId, Long lessonId) {
        Course course = courseQueryService.findCourseById(courseId);

        if (!canUpdateCourse(course)) {
            throw new AccessDeniedException("You do not have permission to update this course");
        }

        course.removeLessonFromSection(sectionId, lessonId);
        return courseRepository.save(course);
    }

    @Override
    public Course updatePrice(Long courseId, MonetaryAmount newPrice) {
        Course course = courseQueryService.findCourseById(courseId);
        course.changePrice(newPrice);

        return courseRepository.save(course);
    }

    private boolean canUpdateCourse(Course course) {
        if (rolesBaseUtil.isAdmin()) {
            return true;
        }

        String currentUserId = rolesBaseUtil.getCurrentSubjectFromJwt();
        return course.getTeacher().equals(currentUserId);
    }

}
