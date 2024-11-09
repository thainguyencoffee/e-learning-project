package com.el.enrollment.application.impl;

import com.el.common.RolesBaseUtil;
import com.el.common.exception.AccessDeniedException;
import com.el.enrollment.application.CourseEnrolmentQueryService;
import com.el.enrollment.application.EnrolmentStatisticService;
import com.el.enrollment.application.dto.CourseInfoDTO;
import com.el.enrollment.application.dto.CourseInfoWithEnrolmentStatisticDTO;
import com.el.enrollment.application.dto.CourseInfoWithEnrolmentsDTO;
import com.el.enrollment.domain.CourseEnrollmentRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EnrolmentStatisticServiceImpl implements EnrolmentStatisticService  {

    private final CourseEnrolmentQueryService courseEnrolmentQueryService;
    private final CourseEnrollmentRepository repository;
    private final RolesBaseUtil rolesBaseUtil;

    public EnrolmentStatisticServiceImpl(CourseEnrolmentQueryService courseEnrolmentQueryService, CourseEnrollmentRepository repository, RolesBaseUtil rolesBaseUtil) {
        this.courseEnrolmentQueryService = courseEnrolmentQueryService;
        this.repository = repository;
        this.rolesBaseUtil = rolesBaseUtil;
    }

    @Override
    public List<CourseInfoWithEnrolmentStatisticDTO> getCourseMinInfoWithEnrolmentStatistics(Pageable pageable) {
        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();
        String currentUser = rolesBaseUtil.getCurrentPreferredUsernameFromJwt();

        if (rolesBaseUtil.isAdmin()) {
            return repository.findAllCourseStatistics(page, size);
        }
        if(rolesBaseUtil.isTeacher()) {
            return repository.findAllCourseStatisticsByTeacher(currentUser, page, size);
        }
        throw new AccessDeniedException("Access denied");
    }

    @Override
    public CourseInfoWithEnrolmentsDTO getCourseWithEnrolmentStatistics(Long courseId) {
        CourseInfoDTO courseInfo = courseEnrolmentQueryService.findCourseWithoutSectionsDTOByIdAndPublished(courseId, true);
        var enrollments = repository.findAllByCourseId(courseId);
        return CourseInfoWithEnrolmentsDTO.of(courseInfo, enrollments);
    }

}
