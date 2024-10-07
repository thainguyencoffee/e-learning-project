package com.el.course.application.impl;

import com.el.common.RolesBaseUtil;
import com.el.common.exception.AccessDeniedException;
import com.el.common.exception.ResourceNotFoundException;
import com.el.course.application.CourseQueryService;
import com.el.course.domain.Course;
import com.el.course.domain.CourseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class CourseQueryServiceImpl implements CourseQueryService {

    private final CourseRepository courseRepository;
    private final RolesBaseUtil rolesBaseUtil;

    public CourseQueryServiceImpl(CourseRepository courseRepository, RolesBaseUtil rolesBaseUtil) {
        this.courseRepository = courseRepository;
        this.rolesBaseUtil = rolesBaseUtil;
    }

    @Override
    public Page<Course> findAllCourses(Pageable pageable) {
        if (rolesBaseUtil.isAdmin()) {
            return courseRepository.findAllByDeleted(false, pageable);
        } else if(rolesBaseUtil.isTeacher()) {
            String teacherId = rolesBaseUtil.getCurrentSubjectFromJwt();
            return courseRepository.findAllByTeacherAndDeleted(teacherId, false, pageable);
        }
        throw new AccessDeniedException("Access denied");
    }

    @Override
    public Course findCourseById(Long courseId) {
        if (rolesBaseUtil.isAdmin()) {
            return courseRepository.findByIdAndDeleted(courseId, false)
                    .orElseThrow(ResourceNotFoundException::new);
        } else if(rolesBaseUtil.isTeacher()) {
            String teacherId = rolesBaseUtil.getCurrentSubjectFromJwt();
            return courseRepository.findByTeacherAndIdAndDeleted(teacherId, courseId, false)
                    .orElseThrow(ResourceNotFoundException::new);
        }
        throw new AccessDeniedException("Access denied");
    }


    @Override
    public Page<Course> findAllPublishedCourses(Pageable pageable) {
        return courseRepository.findAllByPublishedAndDeleted(true, false, pageable);
    }

    @Override
    public Course findPublishedCourseById(Long courseId) {
        return courseRepository.findByIdAndPublishedAndDeleted(courseId, true, false)
                .orElseThrow(ResourceNotFoundException::new);
    }

    @Override
    public Course findCourseDeleted(Long courseId) {
        return courseRepository.findByIdAndDeleted(courseId, true)
                .orElseThrow(ResourceNotFoundException::new);
    }

}
