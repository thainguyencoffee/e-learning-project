package com.el.coursepath.application.impl;

import com.el.common.RolesBaseUtil;
import com.el.common.exception.AccessDeniedException;
import com.el.coursepath.application.CoursePathQueryService;
import com.el.coursepath.application.CoursePathService;
import com.el.coursepath.domain.CoursePath;
import com.el.coursepath.domain.CoursePathRepository;
import com.el.coursepath.web.dto.CoursePathDTO;
import org.springframework.stereotype.Service;

@Service
public class CoursePathServiceImpl implements CoursePathService {

    private final CoursePathQueryService coursePathQueryService;
    private final CoursePathRepository coursePathRepository;
    private final RolesBaseUtil rolesBaseUtil;

    public CoursePathServiceImpl(CoursePathQueryService coursePathQueryService,
                                 CoursePathRepository coursePathRepository, RolesBaseUtil rolesBaseUtil) {
        this.coursePathQueryService = coursePathQueryService;
        this.coursePathRepository = coursePathRepository;
        this.rolesBaseUtil = rolesBaseUtil;
    }

    @Override
    public Long createCoursePath(CoursePathDTO coursePathDTO) {
        if (rolesBaseUtil.isTeacher()) {
            String teacher = rolesBaseUtil.getCurrentPreferredUsernameFromJwt();
            CoursePath coursePath = new CoursePath(coursePathDTO.title(), coursePathDTO.description(), teacher);
            return coursePathRepository.save(coursePath).getId();
        }
        throw new AccessDeniedException("Only teachers can create course paths.");
    }

    @Override
    public void updateCoursePath(Long coursePathId, CoursePathDTO coursePathDTO) {
        CoursePath coursePath = coursePathQueryService.getCoursePath(coursePathId);
        coursePath.updateInfo(coursePathDTO.title(), coursePathDTO.description());

        coursePathRepository.save(coursePath);
    }

    @Override
    public void deleteCoursePath(Long coursePathId) {
        CoursePath coursePath = coursePathQueryService.getCoursePath(coursePathId);
        coursePath.delete();
        coursePathRepository.save(coursePath);
    }

    @Override
    public void deleteCoursePathForce(Long coursePathId) {
        CoursePath coursePath =  coursePathQueryService.getCoursePath(coursePathId, true);
        coursePath.deleteForce();
        coursePathRepository.delete(coursePath);
    }

    @Override
    public void restoreCoursePath(Long coursePathId) {
        CoursePath coursePath = coursePathQueryService.getCoursePath(coursePathId, true);
        coursePath.restore();
        coursePathRepository.save(coursePath);
    }

    @Override
    public Long addCourseOrder(Long coursePathId, Long courseId) {
        CoursePath coursePath = coursePathQueryService.getCoursePath(coursePathId);
        var courseOrder = coursePath.addCourseOrder(courseId);
        coursePathRepository.save(coursePath);
        return courseOrder.getId();
    }

    @Override
    public void removeCourseOrder(Long coursePathId, Long courseOrderId) {
        CoursePath coursePath = coursePathQueryService.getCoursePath(coursePathId);
        coursePath.removeCourseOrder(courseOrderId);
        coursePathRepository.save(coursePath);
    }

    @Override
    public void publishCoursePath(Long coursePathId) {
        CoursePath coursePath = coursePathQueryService.getCoursePath(coursePathId);
        coursePath.publish();
        coursePathRepository.save(coursePath);
    }

    @Override
    public void unpublishCoursePath(Long coursePathId) {
        CoursePath coursePath = coursePathQueryService.getCoursePath(coursePathId);
        coursePath.unpublish();
        coursePathRepository.save(coursePath);
    }
}
