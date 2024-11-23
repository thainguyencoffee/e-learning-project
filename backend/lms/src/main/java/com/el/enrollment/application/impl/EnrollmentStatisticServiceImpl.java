package com.el.enrollment.application.impl;

import com.el.common.RolesBaseUtil;
import com.el.common.exception.AccessDeniedException;
import com.el.enrollment.application.CourseEnrollmentQueryService;
import com.el.enrollment.application.EnrollmentStatisticService;
import com.el.enrollment.application.dto.CourseInfoDTO;
import com.el.enrollment.application.dto.CourseInfoWithEnrolmentStatisticDTO;
import com.el.enrollment.application.dto.CourseInfoWithEnrolmentsDTO;
import com.el.enrollment.domain.EnrollmentRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EnrollmentStatisticServiceImpl implements EnrollmentStatisticService {

    private final CourseEnrollmentQueryService courseEnrollmentQueryService;
    private final EnrollmentRepository repository;
    private final RolesBaseUtil rolesBaseUtil;

    public EnrollmentStatisticServiceImpl(CourseEnrollmentQueryService courseEnrollmentQueryService, EnrollmentRepository repository, RolesBaseUtil rolesBaseUtil) {
        this.courseEnrollmentQueryService = courseEnrollmentQueryService;
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
        CourseInfoDTO courseInfo = courseEnrollmentQueryService.findCourseWithoutSectionsDTOByIdAndPublished(courseId, true);
        var enrollments = repository.findAllByCourseId(courseId);
        return CourseInfoWithEnrolmentsDTO.of(courseInfo, enrollments);
    }

}
