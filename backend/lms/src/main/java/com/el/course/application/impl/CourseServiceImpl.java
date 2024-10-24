package com.el.course.application.impl;

import com.el.common.RolesBaseUtil;
import com.el.common.exception.AccessDeniedException;
import com.el.common.exception.InputInvalidException;
import com.el.course.application.CourseQueryService;
import com.el.course.application.CourseService;
import com.el.course.application.dto.*;
import com.el.course.domain.Course;
import com.el.course.domain.CourseRepository;
import com.el.course.domain.CourseSection;
import com.el.course.domain.Lesson;
import com.el.discount.application.DiscountService;
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
    public void deleteCourseForce(Long courseId) {
        Course course = courseQueryService.findCourseInTrashById(courseId);

        if (!canUpdateCourse(course)) {
            throw new AccessDeniedException("You do not have permission to update this course");
        }

        course.deleteForce();
        courseRepository.delete(course);
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

    @Override
    public void requestPublish(Long courseId, CourseRequestDTO courseRequestDTO) {
        Course course = courseQueryService.findCourseById(courseId);
        course.requestPublish(courseRequestDTO.toCourseRequest());
        courseRepository.save(course);
    }

    @Override
    public void requestUnpublish(Long courseId, CourseRequestDTO courseRequestDTO) {
        Course course = courseQueryService.findCourseById(courseId);
        course.requestUnpublish(courseRequestDTO.toCourseRequest());
        courseRepository.save(course);
    }

    @Override
    public void approvePublish(Long courseId, Long courseRequestId, CourseRequestResolveDTO resolveDTO) {
        Course course = courseQueryService.findCourseById(courseId);
        course.approvePublish(courseRequestId, resolveDTO.resolvedBy(), resolveDTO.message());
        courseRepository.save(course);
    }

    @Override
    public void rejectPublish(Long courseId, Long courseRequestId, CourseRequestResolveDTO resolveDTO) {
        Course course = courseQueryService.findCourseById(courseId);
        course.rejectPublish(courseRequestId, resolveDTO.resolvedBy(), resolveDTO.message());
        courseRepository.save(course);
    }

    @Override
    public void approveUnpublish(Long courseId, Long courseRequestId, CourseRequestResolveDTO resolveDTO) {
        Course course = courseQueryService.findCourseById(courseId);
        course.approveUnpublish(courseRequestId, resolveDTO.resolvedBy(), resolveDTO.message());
        courseRepository.save(course);
    }

    @Override
    public void rejectUnpublish(Long courseId, Long courseRequestId, CourseRequestResolveDTO resolveDTO) {
        Course course = courseQueryService.findCourseById(courseId);
        course.rejectUnpublish(courseRequestId, resolveDTO.resolvedBy(), resolveDTO.message());
        courseRepository.save(course);
    }

}
