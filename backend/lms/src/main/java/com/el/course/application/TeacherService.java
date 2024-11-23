package com.el.course.application;

import com.el.common.RolesBaseUtil;
import com.el.common.auth.application.impl.KeycloakUsersManagement;
import com.el.common.auth.web.dto.UserInfo;
import com.el.common.exception.InputInvalidException;
import com.el.common.projection.MonthStats;
import com.el.common.projection.RatingMonthStats;
import com.el.course.application.dto.teacher.*;
import com.el.course.domain.CourseRepository;
import com.el.enrollment.domain.EnrollmentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class TeacherService {

    private final KeycloakUsersManagement keycloakUsersManagement;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final RolesBaseUtil rolesBaseUtil;

    public TeacherService(KeycloakUsersManagement keycloakUsersManagement, CourseRepository courseRepository,
                          EnrollmentRepository enrollmentRepository, RolesBaseUtil rolesBaseUtil) {
        this.keycloakUsersManagement = keycloakUsersManagement;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.rolesBaseUtil = rolesBaseUtil;
    }

    public List<TeacherDTO> getAllTeacherInfoAndCountData(Pageable pageable) {
        List<UserInfo> userInfoList = keycloakUsersManagement.searchByRole("teacher", pageable)
                .stream().map(UserInfo::fromUserRepresentation)
                .toList();
        log.info("TeacherService found {} teachers", userInfoList.size());
        return userInfoList.stream().map(info -> {
            CountDataDTO countData = courseRepository.getCountDataDTOByTeacher(info.username());
            return new TeacherDTO(info, countData);
        }).toList();
    }

    public TeacherDetailDTO statsTeacherByUsernameAndYear(String teacher, Integer year, Pageable pageable) {
        String currentUsername = rolesBaseUtil.getCurrentPreferredUsernameFromJwt();
        if (rolesBaseUtil.isTeacher() && !currentUsername.equals(teacher)) {
            throw new InputInvalidException("Access denied");
        }
        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();

        UserInfo userInfo = UserInfo.fromUserRepresentation(keycloakUsersManagement.getUser(teacher));
        List<MonthStats> coursesPublishedByMonth = courseRepository.statsMonthPublishedCourseByTeacherAndYear(teacher, year);
        List<MonthStats> studentsEnrolledByMonth = enrollmentRepository.statsMonthEnrolledByTeacherAndYear(teacher, year);
        List<MonthStats> draftCoursesByMonth = courseRepository.statsMonthDraftCourseByTeacherAndYear(teacher, year);
        List<StudentsByCourseDTO> studentsByCourse = enrollmentRepository.statsStudentsByCourse(teacher, page, size);
        List<RatingMonthStats> ratingOverallByMonth = courseRepository.statsMonthRatingOverallByTeacherAndYear(teacher, year);
        return new TeacherDetailDTO(userInfo,
                new TeacherStatisticsDTO(
                        coursesPublishedByMonth,
                        draftCoursesByMonth,
                        studentsEnrolledByMonth,
                        studentsByCourse,
                        ratingOverallByMonth));
    }
}
