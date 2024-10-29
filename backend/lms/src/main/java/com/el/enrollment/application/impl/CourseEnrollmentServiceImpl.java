package com.el.enrollment.application.impl;

import com.el.common.RolesBaseUtil;
import com.el.common.exception.AccessDeniedException;
import com.el.common.exception.ResourceNotFoundException;
import com.el.course.application.CourseQueryService;
import com.el.course.domain.Course;
import com.el.enrollment.application.CourseEnrollmentService;
import com.el.enrollment.domain.CourseEnrollment;
import com.el.enrollment.domain.CourseEnrollmentRepository;
import com.el.enrollment.domain.LessonProgress;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CourseEnrollmentServiceImpl implements CourseEnrollmentService {

    private final CourseEnrollmentRepository repository;
    private final CourseQueryService courseQueryService;
    private final RolesBaseUtil rolesBaseUtil;

    public CourseEnrollmentServiceImpl(CourseEnrollmentRepository repository,
                                       CourseQueryService courseQueryService,
                                       RolesBaseUtil rolesBaseUtil) {
        this.repository = repository;
        this.courseQueryService = courseQueryService;
        this.rolesBaseUtil = rolesBaseUtil;
    }

    @Override
    public Page<CourseEnrollment> findAllCourseEnrollments(Pageable pageable) {
        if (rolesBaseUtil.isAdmin()) {
            return repository.findAll(pageable);
        } else if (rolesBaseUtil.isUser()) {
            String student = rolesBaseUtil.getCurrentPreferredUsernameFromJwt();
            return repository.findAllByStudent(student, pageable);
        }
        throw new AccessDeniedException("Access denied");
    }

    @Override
    public CourseEnrollment findCourseEnrollmentById(Long id) {
        if (rolesBaseUtil.isAdmin()) {
            return repository.findById(id)
                    .orElseThrow(ResourceNotFoundException::new);
        } else if (rolesBaseUtil.isUser()) {
            String student = rolesBaseUtil.getCurrentPreferredUsernameFromJwt();
            return repository.findByIdAndStudent(id, student)
                    .orElseThrow(ResourceNotFoundException::new);
        }
        throw new AccessDeniedException("Access denied");
    }

    public void enrollment(String student, Long courseId) {
        Course course = courseQueryService.findPublishedCourseById(courseId);
        Set<LessonProgress> lessonProgresses = course.getLessonIds()
                .stream()
                .map(LessonProgress::new)
                .collect(Collectors.toSet());

        CourseEnrollment enrollment = new CourseEnrollment(student, courseId, lessonProgresses);
        repository.save(enrollment);
    }

    @Override
    public void markLessonAsCompleted(Long enrollmentId, Long lessonId) {
        CourseEnrollment enrollment = findCourseEnrollmentById(enrollmentId);
        enrollment.markLessonAsCompleted(lessonId);
        repository.save(enrollment);
    }

    @Override
    public void markLessonAsIncomplete(Long enrollmentId, Long lessonId) {
        CourseEnrollment enrollment = findCourseEnrollmentById(enrollmentId);
        enrollment.markLessonAsIncomplete(lessonId);
        repository.save(enrollment);
    }

}
