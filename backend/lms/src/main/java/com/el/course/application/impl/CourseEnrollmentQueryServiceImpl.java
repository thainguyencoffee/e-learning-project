package com.el.course.application.impl;

import com.el.common.RolesBaseUtil;
import com.el.common.exception.AccessDeniedException;
import com.el.common.exception.ResourceNotFoundException;
import com.el.course.domain.CourseRepository;
import com.el.enrollment.application.CourseEnrollmentQueryService;
import com.el.enrollment.application.dto.CourseInfoDTO;
import org.springframework.stereotype.Service;

@Service
public class CourseEnrollmentQueryServiceImpl implements CourseEnrollmentQueryService {

    private final RolesBaseUtil rolesBaseUtil;
    private final CourseRepository courseRepository;

    public CourseEnrollmentQueryServiceImpl(RolesBaseUtil rolesBaseUtil, CourseRepository courseRepository) {
        this.rolesBaseUtil = rolesBaseUtil;
        this.courseRepository = courseRepository;
    }

    @Override
    public CourseInfoDTO findCourseWithoutSectionsDTOByIdAndPublished(long courseId, boolean published) {
        String currentUser = rolesBaseUtil.getCurrentPreferredUsernameFromJwt();
        if (rolesBaseUtil.isAdmin()) {
            return courseRepository.findCourseInfoDTOByIdAndPublishedAndTeacher(courseId, published)
                    .orElseThrow(ResourceNotFoundException::new);
        }
        if (rolesBaseUtil.isTeacher()) {
            return courseRepository.findCourseInfoDTOByIdAndPublishedAndTeacher(courseId, published, currentUser)
                    .orElseThrow(ResourceNotFoundException::new);
        }
        throw new AccessDeniedException("Access denied");
    }

}
